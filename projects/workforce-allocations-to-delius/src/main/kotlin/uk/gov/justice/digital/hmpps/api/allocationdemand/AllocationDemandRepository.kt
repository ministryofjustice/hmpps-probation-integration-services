package uk.gov.justice.digital.hmpps.api.allocationdemand

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.security.ServiceContext
import java.sql.Date

@Repository
class AllocationDemandRepository(val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun findAllocationDemand(params: List<Pair<String, String>>): List<AllocationResponse> {
        jdbcTemplate.update("call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(:dbName)",
            MapSqlParameterSource().addValue("dbName",ServiceContext.servicePrincipal()!!.username)
        )
        return jdbcTemplate.query(
            QS_ALLOCATION_DEMAND,
            MapSqlParameterSource()
                .addValue("values", params.map { arrayOf(it.first, it.second) }),
            mapper
        )
    }

    private val mapper = RowMapper<AllocationResponse> { rs, _ ->
        val sentenceDate: Date? = rs.getDate("sentence_date")
        val iad: Date? = rs.getDate("initial_appointment_date")
        AllocationResponse(
            rs.getString("crn"),
            Name(rs.getString("forename"), rs.getString("middle_name"), rs.getString("surname")),
            Event(
                rs.getString("event_number"),
                EventManager(
                    rs.getString("staff_code"),
                    Name(
                        rs.getString("staff_forename"),
                        rs.getString("staff_middle_name"),
                        rs.getString("staff_surname")
                    ),
                    rs.getString("team_code")
                )
            ),
            if (sentenceDate == null) null
            else Sentence(
                rs.getString("sentence_type"),
                rs.getDate("sentence_date").toLocalDate(),
                "${rs.getString("sentence_length_value")} ${rs.getString("sentence_length_unit")}"
            ),
            if (iad == null) null else InitialAppointment(iad.toLocalDate())
        )
    }
}

const val QS_ALLOCATION_DEMAND = """
SELECT o.CRN                      crn,
       o.FIRST_NAME               forename,
       o.SECOND_NAME              middle_name,
       o.SURNAME                  surname,
       e.EVENT_NUMBER             event_number,
       s.OFFICER_CODE             staff_code,
       s.FORENAME                 staff_forename,
       s.FORENAME2                staff_middle_name,
       s.SURNAME                  staff_surname,
       t.CODE                     team_code,
       dt.DESCRIPTION             sentence_type,
       d.DISPOSAL_DATE            sentence_date,
       d.LENGTH                   sentence_length_value,
       du.CODE_DESCRIPTION        sentence_length_unit,
       (SELECT max(to_date(to_char(c.CONTACT_DATE, 'yyyy-mm-dd') || to_char(c.CONTACT_START_TIME, 'hh24:mi:ss'),
                           'yyyy-mm-dd hh24:mi:ss'))
        FROM CONTACT c
                 JOIN R_CONTACT_TYPE ct ON ct.CONTACT_TYPE_ID = c.CONTACT_TYPE_ID
        WHERE c.EVENT_ID = e.EVENT_ID
          AND ct.CODE IN ('COAI', 'COVI', 'CODI', 'COHV')
          AND c.SOFT_DELETED = 0
          AND e.SOFT_DELETED = 0) initial_appointment_date
FROM OFFENDER o
         JOIN EVENT e ON e.OFFENDER_ID = o.OFFENDER_ID AND e.ACTIVE_FLAG = 1
         JOIN ORDER_MANAGER om ON om.EVENT_ID = e.EVENT_ID AND om.ACTIVE_FLAG = 1
         JOIN TEAM t ON t.TEAM_ID = om.ALLOCATION_TEAM_ID
         JOIN STAFF s ON s.STAFF_ID = om.ALLOCATION_STAFF_ID
         LEFT OUTER JOIN DISPOSAL d ON d.EVENT_ID = e.EVENT_ID AND d.SOFT_DELETED = 0
         LEFT OUTER JOIN R_DISPOSAL_TYPE dt ON dt.DISPOSAL_TYPE_ID = d.DISPOSAL_TYPE_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST du ON du.STANDARD_REFERENCE_LIST_ID = d.ENTRY_LENGTH_UNITS_ID
WHERE (o.CRN, e.EVENT_NUMBER) IN (:values)
  AND e.SOFT_DELETED = 0
  AND om.SOFT_DELETED = 0
"""
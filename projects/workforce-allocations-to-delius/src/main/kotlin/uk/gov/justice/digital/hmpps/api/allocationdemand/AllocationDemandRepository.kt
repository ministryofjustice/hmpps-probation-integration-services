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
        jdbcTemplate.update(
            "call PKG_VPD_CTX.SET_CLIENT_IDENTIFIER(:dbName)",
            MapSqlParameterSource().addValue("dbName", ServiceContext.servicePrincipal()!!.username)
        )
        return jdbcTemplate.query(
            QS_ALLOCATION_DEMAND,
            MapSqlParameterSource()
                .addValue("values", params.map { arrayOf(it.first, it.second) }),
            mapper
        )
    }

    companion object {
        private val gradeMap: Map<String, String> =
            mapOf("PSQ" to "PSO", "PSP" to "PQiP", "PSM" to "PO", "PSC" to "SPO")
        val mapper = RowMapper<AllocationResponse> { rs, _ ->
            val sentenceDate: Date? = rs.getDate("sentence_date")
            val iad: Date? = rs.getDate("initial_appointment_date")
            val managementStatus = ManagementStatus.valueOf(rs.getString("management_status"))
            val managerCode = rs.getString("previous_staff_code")
            AllocationResponse(
                rs.getString("crn"),
                Name(rs.getString("forename"), rs.getString("middle_name"), rs.getString("surname")),
                Event(
                    rs.getString("event_number"),
                    Manager(
                        rs.getString("staff_code"),
                        Name(
                            rs.getString("staff_forename"),
                            rs.getString("staff_middle_name"),
                            rs.getString("staff_surname")
                        ),
                        rs.getString("team_code"),
                        gradeMap[rs.getString("order_manager_grade")]
                    )
                ),
                if (sentenceDate == null) null
                else Sentence(
                    rs.getString("sentence_type"),
                    rs.getDate("sentence_date").toLocalDate(),
                    "${rs.getString("sentence_length_value")} ${rs.getString("sentence_length_unit")}"
                ),
                if (iad == null) null else InitialAppointment(iad.toLocalDate()),
                CaseType.valueOf(rs.getString("case_type")),
                ProbationStatus(managementStatus),
                if (managerCode.endsWith("U")) null else
                    Manager(
                        managerCode,
                        Name(
                            rs.getString("previous_staff_forename"),
                            rs.getString("previous_staff_middle_name"),
                            rs.getString("previous_staff_surname")
                        ),
                        rs.getString("previous_team_code")
                    )
            )
        }
    }
}

const val QS_ALLOCATION_DEMAND = """
WITH previous AS
         (SELECT pcm.OFFENDER_ID,
                 ps.OFFICER_CODE,
                 ps.FORENAME,
                 ps.FORENAME2,
                 ps.SURNAME,
                 pt.CODE,
                 ROW_NUMBER() OVER (PARTITION BY pcm.OFFENDER_ID ORDER BY pcm.END_DATE DESC ) AS row_number
          FROM OFFENDER_MANAGER pcm
                   JOIN STAFF ps ON pcm.ALLOCATION_STAFF_ID = ps.STAFF_ID
                   JOIN TEAM pt ON pt.TEAM_ID = pcm.TEAM_ID
          WHERE pcm.ACTIVE_FLAG = 0)
SELECT o.CRN                                                       crn,
       o.FIRST_NAME                                                forename,
       o.SECOND_NAME                                               middle_name,
       o.SURNAME                                                   surname,
       e.EVENT_NUMBER                                              event_number,
       s.OFFICER_CODE                                              staff_code,
       s.FORENAME                                                  staff_forename,
       s.FORENAME2                                                 staff_middle_name,
       s.SURNAME                                                   staff_surname,
       t.CODE                                                      team_code,
       dt.DESCRIPTION                                              sentence_type,
       d.DISPOSAL_DATE                                             sentence_date,
       d.LENGTH                                                    sentence_length_value,
       du.CODE_DESCRIPTION                                         sentence_length_unit,
       (SELECT max(to_date(to_char(c.CONTACT_DATE, 'yyyy-mm-dd') || to_char(c.CONTACT_START_TIME, 'hh24:mi:ss'),
                           'yyyy-mm-dd hh24:mi:ss'))
        FROM CONTACT c
                 JOIN R_CONTACT_TYPE ct ON ct.CONTACT_TYPE_ID = c.CONTACT_TYPE_ID
        WHERE c.EVENT_ID = e.EVENT_ID
          AND ct.CODE IN ('COAI', 'COVI', 'CODI', 'COHV')
          AND c.SOFT_DELETED = 0
          AND e.SOFT_DELETED = 0)                                  initial_appointment_date,
       CASE
           WHEN o.CURRENT_DISPOSAL = 1 AND cms.OFFICER_CODE NOT LIKE '%U' THEN 'CURRENTLY_MANAGED'
           WHEN EXISTS(SELECT 1
                       FROM DISPOSAL d
                       WHERE d.OFFENDER_ID = o.OFFENDER_ID
                       AND d.EVENT_ID <> e.EVENT_ID) THEN 'PREVIOUSLY_MANAGED'
           ELSE 'NEW_TO_PROBATION' END                             management_status,
       sg.CODE_VALUE                                               order_manager_grade,
       (SELECT type
        FROM (SELECT CASE
                         WHEN l_dt.SENTENCE_TYPE IN ('SC', 'NC') AND
                              l_cs.CODE_VALUE NOT IN ('A', 'C', 'D', 'R', 'I', 'AT') THEN
                             'LICENSE'
                         WHEN l_dt.SENTENCE_TYPE IN ('SC', 'NC') AND l_cs.CODE_VALUE IN ('A', 'C', 'D', 'R', 'I', 'AT')
                             THEN
                             'CUSTODY'
                         WHEN l_dt.SENTENCE_TYPE IN ('SP', 'NP') THEN
                             'COMMUNITY'
                         ELSE
                             'UNKNOWN' END AS                                       type,
                     GREATEST(l_d.NOTIONAL_END_DATE, l_d.ENTERED_NOTIONAL_END_DATE) end_date,
                     l_d.DISPOSAL_DATE                                              start_date
              FROM DISPOSAL l_d
                       JOIN R_DISPOSAL_TYPE l_dt ON l_dt.DISPOSAL_TYPE_ID = l_d.DISPOSAL_TYPE_ID
                       LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST l_u
                                       ON l_u.STANDARD_REFERENCE_LIST_ID = l_d.ENTRY_LENGTH_UNITS_ID
                       LEFT OUTER JOIN CUSTODY l_c ON l_c.DISPOSAL_ID = l_d.DISPOSAL_ID
                       LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST l_cs
                                       ON l_cs.STANDARD_REFERENCE_LIST_ID = l_c.CUSTODIAL_STATUS_ID
              WHERE l_d.OFFENDER_ID = o.OFFENDER_ID
                AND l_d.ACTIVE_FLAG = 1
                AND l_d.SOFT_DELETED = 0)
        ORDER BY end_date DESC, start_date FETCH NEXT 1 ROWS ONLY) case_type,
       previous.OFFENDER_ID                                        previous_offender_id,
       previous.OFFICER_CODE                                       previous_staff_code,
       previous.FORENAME                                           previous_staff_forename,
       previous.FORENAME2                                          previous_staff_middle_name,
       previous.SURNAME                                            previous_staff_surname,
       previous.CODE                                               previous_team_code

FROM OFFENDER o
         JOIN EVENT e ON e.OFFENDER_ID = o.OFFENDER_ID AND e.ACTIVE_FLAG = 1
         JOIN OFFENDER_MANAGER cm ON cm.OFFENDER_ID = o.OFFENDER_ID AND cm.ACTIVE_FLAG = 1
         JOIN STAFF cms ON cm.ALLOCATION_STAFF_ID = cms.STAFF_ID
         JOIN ORDER_MANAGER om ON om.EVENT_ID = e.EVENT_ID AND om.ACTIVE_FLAG = 1
         JOIN TEAM t ON t.TEAM_ID = om.ALLOCATION_TEAM_ID
         JOIN STAFF s ON s.STAFF_ID = om.ALLOCATION_STAFF_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST sg ON s.STAFF_GRADE_ID = sg.STANDARD_REFERENCE_LIST_ID
         LEFT OUTER JOIN DISPOSAL d ON d.EVENT_ID = e.EVENT_ID AND d.ACTIVE_FLAG = 1
         LEFT OUTER JOIN R_DISPOSAL_TYPE dt ON dt.DISPOSAL_TYPE_ID = d.DISPOSAL_TYPE_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST du ON du.STANDARD_REFERENCE_LIST_ID = d.ENTRY_LENGTH_UNITS_ID
         LEFT OUTER JOIN CUSTODY c ON c.DISPOSAL_ID = d.DISPOSAL_ID AND c.SOFT_DELETED = 0
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST cs ON cs.STANDARD_REFERENCE_LIST_ID = c.CUSTODIAL_STATUS_ID
         LEFT OUTER JOIN previous ON previous.OFFENDER_ID = o.OFFENDER_ID AND previous.row_number = 1
WHERE (o.CRN, e.EVENT_NUMBER) IN (:values)
  AND e.SOFT_DELETED = 0
  AND d.SOFT_DELETED = 0
  AND om.SOFT_DELETED = 0
"""

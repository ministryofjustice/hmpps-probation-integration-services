package uk.gov.justice.digital.hmpps.api.allocationdemand

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.AllocationResponse
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.CaseType
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.Event
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.InitialAppointment
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.ManagementStatus
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.Manager
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.Name
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.ProbationStatus
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.Sentence
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
            val managerCode = rs.getString("community_manager_code")
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
                        rs.getString("team_code")
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
                            rs.getString("community_manager_forename"),
                            rs.getString("community_manager_middle_name"),
                            rs.getString("community_manager_surname")
                        ),
                        rs.getString("community_manager_team_code"),
                        gradeMap[rs.getString("community_manager_grade")]
                    )
            )
        }
    }
}

const val QS_ALLOCATION_DEMAND = """
SELECT o.CRN                                                            crn,
       o.FIRST_NAME                                                     forename,
       o.SECOND_NAME                                                    middle_name,
       o.SURNAME                                                        surname,
       e.EVENT_NUMBER                                                   event_number,
       s.OFFICER_CODE                                                   staff_code,
       s.FORENAME                                                       staff_forename,
       s.FORENAME2                                                      staff_middle_name,
       s.SURNAME                                                        staff_surname,
       t.CODE                                                           team_code,
       dt.DESCRIPTION                                                   sentence_type,
       d.DISPOSAL_DATE                                                  sentence_date,
       d.ENTRY_LENGTH                                                   sentence_length_value,
       du.CODE_DESCRIPTION                                              sentence_length_unit,
       (SELECT max(to_date(to_char(c.CONTACT_DATE, 'yyyy-mm-dd') || to_char(c.CONTACT_START_TIME, 'hh24:mi:ss'),
                           'yyyy-mm-dd hh24:mi:ss'))
        FROM CONTACT c
                 JOIN R_CONTACT_TYPE ct ON ct.CONTACT_TYPE_ID = c.CONTACT_TYPE_ID
        WHERE c.EVENT_ID = e.EVENT_ID
          AND ct.CODE IN ('COAI', 'COVI', 'CODI', 'COHV')
          AND c.SOFT_DELETED = 0
          AND e.SOFT_DELETED = 0)                                       initial_appointment_date,
       CASE
           WHEN EXISTS(SELECT 1
                       FROM DISPOSAL od
                                JOIN EVENT oe ON oe.EVENT_ID = od.EVENT_ID
                                JOIN ORDER_MANAGER oom ON oom.EVENT_ID = oe.EVENT_ID AND oom.ACTIVE_FLAG = 1
                                JOIN STAFF os ON os.STAFF_ID = oom.ALLOCATION_STAFF_ID
                       WHERE od.OFFENDER_ID = o.OFFENDER_ID
                         AND od.ACTIVE_FLAG = 1
                         AND os.OFFICER_CODE LIKE '%U')
               AND (SELECT COUNT(1)
                    FROM DISPOSAL od
                    WHERE od.OFFENDER_ID = o.OFFENDER_ID
                      AND od.DISPOSAL_ID <> d.DISPOSAL_ID) = 0 THEN 'NEW_TO_PROBATION'
           WHEN EXISTS(SELECT 1
                       FROM DISPOSAL od
                                JOIN EVENT oe ON oe.EVENT_ID = od.EVENT_ID
                                JOIN ORDER_MANAGER oom ON oom.EVENT_ID = oe.EVENT_ID AND oom.ACTIVE_FLAG = 1
                                JOIN STAFF os ON os.STAFF_ID = oom.ALLOCATION_STAFF_ID
                       WHERE od.OFFENDER_ID = o.OFFENDER_ID
                         AND od.ACTIVE_FLAG = 1
                         AND oom.SOFT_DELETED = 0
                         AND os.OFFICER_CODE NOT LIKE '%U') THEN 'CURRENTLY_MANAGED'
           WHEN EXISTS(SELECT 1
                       FROM DISPOSAL od
                       WHERE od.OFFENDER_ID = o.OFFENDER_ID
                         AND od.ACTIVE_FLAG = 0)
               AND NOT EXISTS(SELECT 1
                              FROM DISPOSAL od
                                       JOIN EVENT oe ON oe.EVENT_ID = od.EVENT_ID
                                       JOIN ORDER_MANAGER oom ON oom.EVENT_ID = oe.EVENT_ID AND oom.ACTIVE_FLAG = 1
                                       JOIN STAFF os ON os.STAFF_ID = oom.ALLOCATION_STAFF_ID
                              WHERE od.OFFENDER_ID = o.OFFENDER_ID
                                AND od.DISPOSAL_ID <> d.DISPOSAL_ID
                                AND od.ACTIVE_FLAG = 1
                                AND od.SOFT_DELETED = 0
                                AND os.OFFICER_CODE NOT LIKE '%U') THEN 'PREVIOUSLY_MANAGED'
           ELSE 'UNKNOWN' END                                           management_status,
       (SELECT type
        FROM (SELECT CASE
                         WHEN l_dt.SENTENCE_TYPE = 'SC' AND
                              l_cs.CODE_VALUE NOT IN ('A', 'C', 'D', 'R', 'I', 'AT') THEN
                             'LICENSE'
                         WHEN l_dt.SENTENCE_TYPE IN ('SC', 'NC') AND l_cs.CODE_VALUE IN ('A', 'C', 'D', 'R', 'I', 'AT')
                             THEN
                             'CUSTODY'
                         WHEN l_dt.SENTENCE_TYPE = 'SP' THEN
                             'COMMUNITY'
                         ELSE
                             'UNKNOWN' END AS                                                                  type,
                     GREATEST(
                             NVL(l_d.NOTIONAL_END_DATE, TO_DATE('1970-01-01', 'YYYY-MM-DD')),
                             NVL(l_d.ENTERED_NOTIONAL_END_DATE, TO_DATE('1970-01-01', 'YYYY-MM-DD')),
                             NVL((SELECT KEY_DATE
                                  FROM KEY_DATE kd
                                           JOIN R_STANDARD_REFERENCE_LIST kdt
                                                on kdt.STANDARD_REFERENCE_LIST_ID = kd.KEY_DATE_TYPE_ID
                                  WHERE kdt.CODE_VALUE = 'SED'
                                    AND l_c.CUSTODY_ID = kd.CUSTODY_ID), TO_DATE('1970-01-01', 'YYYY-MM-DD'))) end_date,
                     l_d.DISPOSAL_DATE                                                                         start_date
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
        ORDER BY end_date DESC, start_date DESC FETCH NEXT 1 ROWS ONLY) case_type,
       cms.OFFICER_CODE                                                 community_manager_code,
       cms.FORENAME                                                     community_manager_forename,
       cms.FORENAME2                                                    community_manager_middle_name,
       cms.SURNAME                                                      community_manager_surname,
       cmt.CODE                                                         community_manager_team_code,
       cmsg.CODE_VALUE                                                  community_manager_grade

FROM OFFENDER o
         JOIN EVENT e ON e.OFFENDER_ID = o.OFFENDER_ID AND e.ACTIVE_FLAG = 1
         JOIN OFFENDER_MANAGER cm ON cm.OFFENDER_ID = o.OFFENDER_ID AND cm.ACTIVE_FLAG = 1
         JOIN STAFF cms ON cm.ALLOCATION_STAFF_ID = cms.STAFF_ID
         JOIN TEAM cmt ON cmt.TEAM_ID = cm.TEAM_ID
         JOIN ORDER_MANAGER om ON om.EVENT_ID = e.EVENT_ID AND om.ACTIVE_FLAG = 1
         JOIN TEAM t ON t.TEAM_ID = om.ALLOCATION_TEAM_ID
         JOIN STAFF s ON s.STAFF_ID = om.ALLOCATION_STAFF_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST cmsg ON cms.STAFF_GRADE_ID = cmsg.STANDARD_REFERENCE_LIST_ID
         LEFT OUTER JOIN DISPOSAL d ON d.EVENT_ID = e.EVENT_ID AND d.ACTIVE_FLAG = 1
         LEFT OUTER JOIN R_DISPOSAL_TYPE dt ON dt.DISPOSAL_TYPE_ID = d.DISPOSAL_TYPE_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST du ON du.STANDARD_REFERENCE_LIST_ID = d.ENTRY_LENGTH_UNITS_ID
         LEFT OUTER JOIN CUSTODY c ON c.DISPOSAL_ID = d.DISPOSAL_ID AND c.SOFT_DELETED = 0
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST cs ON cs.STANDARD_REFERENCE_LIST_ID = c.CUSTODIAL_STATUS_ID
WHERE (o.CRN, e.EVENT_NUMBER) IN (:values)
  AND e.SOFT_DELETED = 0
  AND d.SOFT_DELETED = 0
  AND om.SOFT_DELETED = 0
"""

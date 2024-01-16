package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.api.model.AllocationResponse
import uk.gov.justice.digital.hmpps.api.model.CaseType
import uk.gov.justice.digital.hmpps.api.model.Event
import uk.gov.justice.digital.hmpps.api.model.InitialAppointment
import uk.gov.justice.digital.hmpps.api.model.ManagementStatus
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.NamedCourt
import uk.gov.justice.digital.hmpps.api.model.ProbationStatus
import uk.gov.justice.digital.hmpps.api.model.Sentence
import uk.gov.justice.digital.hmpps.api.model.StaffMember
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
                if (sentenceDate == null) {
                    null
                } else {
                    Sentence(
                        rs.getString("sentence_type"),
                        rs.getDate("sentence_date").toLocalDate(),
                        "${rs.getString("sentence_length_value")} ${rs.getString("sentence_length_unit")}"
                    )
                },
                if (iad == null) {
                    null
                } else {
                    InitialAppointment(
                        iad.toLocalDate(),
                        StaffMember(
                            rs.getString("ias_code"),
                            Name(
                                rs.getString("ias_forename"),
                                rs.getString("ias_middle_name"),
                                rs.getString("ias_surname")
                            ),
                            grade = rs.getString("ias_grade")
                        )
                    )
                },
                NamedCourt(rs.getString("court_name")),
                CaseType.valueOf(rs.getString("case_type")),
                ProbationStatus(managementStatus),
                if (managerCode.endsWith("U")) {
                    null
                } else {
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
                },
                rs.getDate("com_handover_date").toLocalDate()
            )
        }
    }
}

const val QS_ALLOCATION_DEMAND = """
WITH ORIGIN_COURT AS (SELECT e.EVENT_ID,
                             c.COURT_NAME,
                             ROW_NUMBER() OVER (partition by e.EVENT_ID order by ca.APPEARANCE_DATE) row_num
                      FROM COURT_APPEARANCE ca
                               JOIN R_STANDARD_REFERENCE_LIST at
                                    ON at.STANDARD_REFERENCE_LIST_ID = ca.APPEARANCE_TYPE_ID
                               JOIN COURT c ON ca.COURT_ID = c.COURT_ID
                               JOIN EVENT e ON e.EVENT_ID = ca.EVENT_ID AND e.ACTIVE_FLAG = 1
                               JOIN OFFENDER o ON o.OFFENDER_ID = ca.OFFENDER_ID
                      WHERE (o.CRN, e.EVENT_NUMBER) in (:values)
                        AND at.CODE_VALUE = 'S'
                        AND ca.APPEARANCE_DATE < current_date
                        AND ca.OUTCOME_ID is not null
                        AND e.SOFT_DELETED = 0
                        AND ca.SOFT_DELETED = 0),
     INITIAL_APPOINTMENT as (
         (SELECT ia.CONTACT_ID,
                 ia.EVENT_ID,
                 ia.STAFF_ID,
                 to_date(to_char(ia.CONTACT_DATE, 'yyyy-mm-dd') || ' ' || to_char(ia.CONTACT_START_TIME, 'hh24:mi:ss'),
                         'yyyy-mm-dd hh24:mi:ss') as                                                      datetime,
                 row_number() over (partition by ia.EVENT_ID order by to_date(
                             to_char(ia.CONTACT_DATE, 'yyyy-mm-dd') || ' ' ||
                             to_char(ia.CONTACT_START_TIME, 'hh24:mi:ss'), 'yyyy-mm-dd hh24:mi:ss') desc) ROW_NUM
          FROM CONTACT ia
                   JOIN OFFENDER iao ON iao.OFFENDER_ID = ia.OFFENDER_ID
                   JOIN EVENT iae ON iae.EVENT_ID = ia.EVENT_ID
                   JOIN R_CONTACT_TYPE ct ON ct.CONTACT_TYPE_ID = ia.CONTACT_TYPE_ID
          WHERE (iao.CRN, iae.EVENT_NUMBER) IN (:values)
            AND ct.CODE IN ('COAI', 'COVI', 'CODI', 'COHV')
            AND ia.SOFT_DELETED = 0
            AND iao.SOFT_DELETED = 0
            AND iae.SOFT_DELETED = 0))
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
       court.COURT_NAME                                                 court_name,
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
       cmsg.CODE_VALUE                                                  community_manager_grade,
       ia.datetime                                                      initial_appointment_date,
       ias.OFFICER_CODE                                                 ias_code,
       ias.FORENAME                                                     ias_forename,
       ias.FORENAME2                                                    ias_middle_name,
       ias.SURNAME                                                      ias_surname,
       iasg.CODE_DESCRIPTION                                            ias_grade,
       kd.KEY_DATE                                                      com_handover_date
FROM OFFENDER o
         JOIN EVENT e ON e.OFFENDER_ID = o.OFFENDER_ID AND e.ACTIVE_FLAG = 1
         JOIN OFFENDER_MANAGER cm ON cm.OFFENDER_ID = o.OFFENDER_ID AND cm.ACTIVE_FLAG = 1
         JOIN STAFF cms ON cm.ALLOCATION_STAFF_ID = cms.STAFF_ID
         JOIN TEAM cmt ON cmt.TEAM_ID = cm.TEAM_ID
         JOIN ORDER_MANAGER om ON om.EVENT_ID = e.EVENT_ID AND om.ACTIVE_FLAG = 1
         JOIN TEAM t ON t.TEAM_ID = om.ALLOCATION_TEAM_ID
         JOIN STAFF s ON s.STAFF_ID = om.ALLOCATION_STAFF_ID
         JOIN DISPOSAL d ON d.EVENT_ID = e.EVENT_ID AND d.ACTIVE_FLAG = 1
         JOIN R_DISPOSAL_TYPE dt ON dt.DISPOSAL_TYPE_ID = d.DISPOSAL_TYPE_ID
         JOIN ORIGIN_COURT court ON court.EVENT_ID = e.EVENT_ID AND court.row_num = 1
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST cmsg ON cms.STAFF_GRADE_ID = cmsg.STANDARD_REFERENCE_LIST_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST du ON du.STANDARD_REFERENCE_LIST_ID = d.ENTRY_LENGTH_UNITS_ID
         LEFT OUTER JOIN CUSTODY c ON c.DISPOSAL_ID = d.DISPOSAL_ID AND c.SOFT_DELETED = 0
         LEFT OUTER JOIN (SELECT kd.CUSTODY_ID, kd.KEY_DATE 
                          FROM KEY_DATE kd
                          JOIN R_STANDARD_REFERENCE_LIST srl 
                          ON srl.STANDARD_REFERENCE_LIST_ID = kd.KEY_DATE_TYPE_ID
                          AND srl.CODE_VALUE = 'POM2'
                          AND kd.SOFT_DELETED = 0) kd ON kd.CUSTODY_ID = c.CUSTODY_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST cs ON cs.STANDARD_REFERENCE_LIST_ID = c.CUSTODIAL_STATUS_ID
         LEFT OUTER JOIN INITIAL_APPOINTMENT ia ON e.EVENT_ID = ia.EVENT_ID AND ia.ROW_NUM = 1
         LEFT OUTER JOIN STAFF ias ON ias.STAFF_ID = ia.STAFF_ID
         LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST iasg ON ias.STAFF_GRADE_ID = iasg.STANDARD_REFERENCE_LIST_ID
WHERE (o.CRN, e.EVENT_NUMBER) IN (:values)
  AND o.SOFT_DELETED = 0
  AND e.SOFT_DELETED = 0
  AND d.SOFT_DELETED = 0
  AND om.SOFT_DELETED = 0
"""

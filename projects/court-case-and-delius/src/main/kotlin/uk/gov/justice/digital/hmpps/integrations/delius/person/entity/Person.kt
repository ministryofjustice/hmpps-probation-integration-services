package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

interface PersonRepository : JpaRepository<Person, Long> {

    @Query(
        """
        select
           o.crn,
           sum(case when d.active_flag = 1 then 1 else 0 end)                                         as currentCount,
           sum(case when d.active_flag = 0 then 1 else 0 end)                                         as previousCount,
           max(d.termination_date)                                                                    as terminationDate,
           sum(e.in_breach)                                                                           as breachCount,
           sum(case when e.active_flag = 1 and d.disposal_id is null then 1 else 0 end)               as preSentenceCount,
           sum(case when d.disposal_id is null and ca.outcome_code = '101' then 1 else 0 end)         as awaitingPsrCount
        from offender o
             join event e on e.offender_id = o.offender_id and e.soft_deleted = 0
             left join disposal d on d.event_id = e.event_id and d.soft_deleted = 0
             left join (select ca.event_id, oc.code_value as outcome_code
                        from court_appearance ca
                                 join r_standard_reference_list oc on ca.outcome_id = oc.standard_reference_list_id) ca
                       on ca.event_id = e.event_id
        where crn = :crn
            and o.soft_deleted = 0
        group by o.crn
        """,
        nativeQuery = true
    )
    fun statusOf(crn: String): SentenceCounts?

    fun findByCrnAndSoftDeletedIsFalse(crn: String): Person?

    @Query(
        """
            SELECT json_object(
               'crn' VALUE o.crn,
               'offenderManagers' VALUE json_array(json_object(
                                                           'active' VALUE
                                                           CASE
                                                               WHEN om.ACTIVE_FLAG = 1 THEN 'true'
                                                               ELSE 'false' END
                                                           FORMAT JSON,
                                                           'allocationDate' VALUE om.ALLOCATION_DATE,
                                                           'staff' VALUE json_object(
                                                                   'forename' VALUE staff.FORENAME ||
                                                                                    DECODE(staff.FORENAME2, NULL, '', ' ' || staff.FORENAME2),
                                                                   'surname' VALUE staff.SURNAME ABSENT ON NULL),
                                                           'team' VALUE json_object(
                                                                   'description' VALUE team.DESCRIPTION,
                                                                   'telephone' VALUE team.TELEPHONE,
                                                                   'localDeliveryUnit' VALUE ldu.CODE,
                                                                   'district' VALUE district.DESCRIPTION
                                                                   ABSENT ON NULL ),
                                                           'provider' VALUE pa.DESCRIPTION
                                                           ABSENT ON NULL ) ),
               'convictions' VALUE (SELECT json_arrayagg(
                                                   json_object('active' VALUE
                                                               CASE
                                                                   WHEN e.ACTIVE_FLAG = 1 THEN 'true'
                                                                   ELSE 'false' END,
                                                               'inBreach' VALUE
                                                               CASE
                                                                   WHEN e.IN_BREACH = 1 THEN 'true'
                                                                   ELSE 'false' END,
                                                               'convictionDate' VALUE e.CONVICTION_DATE,
                                                               'custodialType' VALUE ct.CODE_DESCRIPTION,
                                                               'offences' VALUE (SELECT json_arrayagg(
                                                                                                json_object(
                                                                                                        'offenceDate'
                                                                                                        VALUE oDate,
                                                                                                        'description'
                                                                                                        VALUE descr,
                                                                                                        'mainOffence'
                                                                                                        VALUE
                                                                                                        mainOffence
                                                                                                        FORMAT JSON))
                                                                                 from (select OFFENCE_DATE as oDate, DESCRIPTION as descr, 'true' as mainOffence
                                                                                       from main_Offence mo
                                                                                                join r_offence ot on ot.OFFENCE_ID = mo.OFFENCE_ID
                                                                                       where mo.EVENT_ID = e.EVENT_ID

                                                                                       UNION ALL

                                                                                       select OFFENCE_DATE as oDate, DESCRIPTION as descr, 'false' as mainOffence
                                                                                       from ADDITIONAL_OFFENCE ao
                                                                                                join r_offence ot on ot.OFFENCE_ID = ao.OFFENCE_ID
                                                                                       where ao.EVENT_ID = e.EVENT_ID)),
                                                               'sentence' VALUE json_object(
                                                                       'description' value dt.CODE_DESCRIPTION,
                                                                       'length' value d.LENGTH,
                                                                       'lengthUnits' value d.ENTRY_LENGTH_UNITS_ID,
                                                                       'lengthInDays' value d.LENGTH_IN_DAYS,
                                                                       'terminationDate' value d.TERMINATION_DATE,
                                                                       'startDate' value d.DISPOSAL_DATE,
                                                                       'endDate' value d.NOTIONAL_END_DATE,
                                                                       'terminationReason' value tr.CODE_DESCRIPTION,
                                                                       'unpaidWork' value CASE
                                                                                              WHEN d.UPW = 1 THEN 'true'
                                                                                              ELSE 'false' END
                                                                   )
                                                               ABSENT ON NULL )
                                                   ABSENT ON NULL )
                                    FROM event e
                                             LEFT OUTER JOIN disposal d on e.EVENT_ID = d.EVENT_ID
                                             LEFT OUTER JOIN CUSTODY c on c.DISPOSAL_ID = d.disposal_id
                                             LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST ct
                                                             ON ct.STANDARD_REFERENCE_LIST_ID = c.CUSTODIAL_STATUS_ID
                                             LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST dt
                                                             ON d.DISPOSAL_TYPE_ID = dt.STANDARD_REFERENCE_LIST_ID
                                             LEFT OUTER JOIN R_STANDARD_REFERENCE_LIST tr
                                                             ON tr.STANDARD_REFERENCE_LIST_ID = DISPOSAL_TERMINATION_REASON_ID

                                    WHERE e.OFFENDER_ID = o.OFFENDER_ID
                                      AND e.SOFT_DELETED = 0
                                      AND d.SOFT_DELETED = 0
                                      AND c.SOFT_DELETED = 0)
           )
FROM offender o
         JOIN OFFENDER_MANAGER om ON om.OFFENDER_ID = o.OFFENDER_ID AND om.ACTIVE_FLAG = 1
         LEFT OUTER JOIN PROBATION_AREA pa ON pa.PROBATION_AREA_ID = om.PROBATION_AREA_ID
         LEFT OUTER JOIN STAFF staff ON staff.STAFF_ID = om.ALLOCATION_STAFF_ID
         LEFT OUTER JOIN TEAM team ON team.TEAM_ID = om.TEAM_ID
         LEFT OUTER JOIN LOCAL_DELIVERY_UNIT ldu ON ldu.LOCAL_DELIVERY_UNIT_ID = team.LOCAL_DELIVERY_UNIT_ID
         LEFT OUTER JOIN DISTRICT district ON district.DISTRICT_ID = team.DISTRICT_ID
WHERE o.SOFT_DELETED = 0
  AND om.SOFT_DELETED = 0
  and o.crn = :crn
        """,
        nativeQuery = true
    )
    fun getProbationRecord(crn: String): String
}

interface SentenceCounts {
    val crn: String
    val currentCount: Int
    val previousCount: Int
    val terminationDate: LocalDate?
    val breachCount: Int
    val preSentenceCount: Int
    val awaitingPsrCount: Int
}

package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.CaseType
import uk.gov.justice.digital.hmpps.api.model.ManagementStatus
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

const val DISPOSAL_SQL = """
    select o.crn, d.active_flag * e.active_flag as active_flag, s.officer_code
    from offender o
    join event e on e.offender_id = o.offender_id and e.soft_deleted = 0
    join order_manager om on om.event_id = e.event_id and om.active_flag = 1 and om.soft_deleted = 0
    join staff s on s.staff_id = om.allocation_staff_id
    join disposal d on d.event_id = e.event_id and d.soft_deleted = 0
    where crn = :crn
"""

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrnAndSoftDeletedFalse(crn: String): Person?
    fun findByNomsIdAndSoftDeletedFalse(nomsId: String): Person?

    @Query("select p from Person p where p.crn in :crns and p.softDeleted = false")
    fun findAllByCrnAndSoftDeletedFalse(crns: List<String>): List<Person>

    @Query("select p.id from Person p where p.crn = :crn")
    fun findIdByCrn(crn: String): Long?

    @Query(
        """
        SELECT COUNT(pt) FROM PersonTransfer pt
        JOIN ReferenceData status ON pt.statusId = status.id 
        WHERE pt.personId = :personId 
        AND status.code = 'PN'
        AND pt.softDeleted = false
    """
    )
    fun countPendingTransfers(personId: Long): Int

    @Modifying
    @Query(
        """
    merge into iaps_offender using dual on (offender_id = ?1) 
    when matched then update set iaps_flag=?2 
    when not matched then insert(offender_id, iaps_flag) values(?1,?2)
    """,
        nativeQuery = true
    )
    fun updateIaps(personId: Long, iapsFlagValue: Long = 1)

    @Query(
        """
        with
            total_disposals as (select count(1) as cnt from ($DISPOSAL_SQL)),
            unallocated_disposals as (select count(1) as cnt from ($DISPOSAL_SQL) where active_flag = 1 and officer_code like '%U'),
            allocated_disposals as (select count(1) as cnt from ($DISPOSAL_SQL) where active_flag = 1 and officer_code not like '%U'),
            previous_disposals as (select count(1) as cnt from ($DISPOSAL_SQL) where active_flag = 0)
        select case
                   when unallocated_disposals.cnt = 1 and total_disposals.cnt = 1 then 'NEW_TO_PROBATION'
                   when allocated_disposals.cnt > 0 then 'CURRENTLY_MANAGED'
                   when previous_disposals.cnt > 0 then 'PREVIOUSLY_MANAGED'
                   else 'UNKNOWN'
               end as managed_status
        from total_disposals,
             allocated_disposals,
             previous_disposals,
             unallocated_disposals
        """,
        nativeQuery = true
    )
    fun getProbationStatus(crn: String): ManagementStatus

    @Query(
        """
        select case
            when sentence_type in ('SC', 'NC') and custodial_status in :custodialStatusCodes then 'CUSTODY'
            when sentence_type = 'SC' then 'LICENSE'
            when sentence_type = 'SP' then 'COMMUNITY'
            else 'UNKNOWN' end as type
        from (select dt.sentence_type,
                     cs.code_value                                                   as custodial_status,
                     greatest(nvl(d.notional_end_date, to_date('1970-01-01', 'YYYY-MM-DD')),
                              nvl(d.entered_notional_end_date, to_date('1970-01-01', 'YYYY-MM-DD')),
                              nvl(kd.key_date, to_date('1970-01-01', 'YYYY-MM-DD'))) as end_date,
                     d.disposal_date                                                 as start_date
              from offender o
                  join event e on e.offender_id = o.offender_id and e.soft_deleted = 0 and e.active_flag = 1
                  join disposal d on d.event_id = e.event_id and d.soft_deleted = 0 and d.active_flag = 1
                  join r_disposal_type dt on dt.disposal_type_id = d.disposal_type_id
                  left join custody c on d.disposal_id = c.disposal_id and c.soft_deleted = 0
                  left join r_standard_reference_list cs on c.custodial_status_id = cs.standard_reference_list_id
                  left join key_date kd on c.custody_id = kd.custody_id and kd.soft_deleted = 0
                  left join r_standard_reference_list kdt on kd.key_date_type_id = kdt.standard_reference_list_id and
                                                          kdt.code_value = :sentenceEndDateKeyDateTypeCode
              where crn = :crn
              order by end_date desc, start_date desc fetch next 1 rows only)
        """,
        nativeQuery = true
    )
    fun findCaseType(
        crn: String,
        sentenceEndDateKeyDateTypeCode: String = "SED",
        custodialStatusCodes: List<String> = listOf("A", "C", "D", "R", "I", "AT")
    ): CaseType?

    @Query(
        """
        select count(r) 
        from Requirement r
        left join r.mainCategory mc
        left join r.subCategory sc
        left join r.additionalMainCategory amc
        where r.person.id = :personId
        and (mc.code = 'RM38' 
            or (mc.code = '7' and (sc is null or sc.code <> 'RS66'))
            or (amc.code in ('RM38', '7')))
        and r.active = true and r.softDeleted = false
    """
    )
    fun countAccreditedProgrammeRequirements(personId: Long): Int

    @Query(
        """
        select o.crn, max(allocation.allocation_date) as allocatedAt
        from ( select offender.offender_id,
                      event.event_number,
                      order_manager.event_id,
                      order_manager.allocation_date,
                      order_manager.allocation_staff_id,
                      created_by.distinguished_name                                           as created_by,
                      lag(order_manager.allocation_staff_id)
                          over (partition by order_manager.event_id order by allocation_date) as prev_staff_id
               from order_manager
               join event on event.event_id = order_manager.event_id and event.soft_deleted = 0
               join disposal on disposal.event_id = event.event_id and disposal.soft_deleted = 0
               join r_disposal_type on r_disposal_type.disposal_type_id = disposal.disposal_type_id
               join offender on offender.offender_id = event.offender_id and offender.soft_deleted = 0
               join user_ created_by on created_by.user_id = order_manager.created_by_user_id
               where order_manager.soft_deleted = 0
                 and offender.crn in (:crns) ) allocation
        join offender o on o.offender_id = allocation.offender_id
        join staff init on init.staff_id = allocation.allocation_staff_id
        join staff prev on prev.staff_id = allocation.prev_staff_id
        where prev.officer_code like '%U'
          and init.officer_code not like '%U'
          and allocation.created_by = 'HMPPSAllocations'
        group by o.crn
    """, nativeQuery = true
    )
    fun findMostRecentInitialAllocations(crns: Set<String>): List<MostRecentInitialAllocation>
}

interface MostRecentInitialAllocation {
    val crn: String
    val allocatedAt: LocalDate?
}

fun PersonRepository.getCaseType(crn: String) = findCaseType(crn) ?: CaseType.UNKNOWN

fun PersonRepository.getByCrnAndSoftDeletedFalse(crn: String) = findByCrnAndSoftDeletedFalse(crn)
    ?: throw NotFoundException("Person", "crn", crn)

fun PersonRepository.getByNomsIdAndSoftDeletedFalse(nomsId: String) = findByNomsIdAndSoftDeletedFalse(nomsId)
    ?: throw NotFoundException("Person", "nomsId", nomsId)

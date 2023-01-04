package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.ManagementStatus

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrnAndSoftDeletedFalse(crn: String): Person?

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
            disposals as (
                select o.crn, d.active_flag * e.active_flag as active_flag, s.officer_code
                from offender o
                join event e on e.offender_id = o.offender_id and e.soft_deleted = 0
                join order_manager om on om.event_id = e.event_id and om.active_flag = 1 and om.soft_deleted = 0
                join staff s on s.staff_id = om.allocation_staff_id
                join disposal d on d.event_id = e.event_id and d.soft_deleted = 0
                where crn = :crn
            ),
            total_disposals as (select count(1) as cnt from disposals),
            unallocated_disposals as (select count(1) as cnt from disposals where active_flag = 1 and officer_code like '%U'),
            allocated_disposals as (select count(1) as cnt from disposals where active_flag = 1 and officer_code not like '%U'),
            previous_disposals as (select count(1) as cnt from disposals where active_flag = 0)
        select case
                   when unallocated_disposals.cnt = 1 and total_disposals.cnt = 1 then 'NEW_TO_PROBATION'
                   when allocated_disposals.cnt > 0 then 'CURRENTLY_MANAGED'
                   when previous_disposals.cnt > 0 then 'PREVIOUSLY_MANAGED'
                   else 'UNKNOWN'
               end as management_status
        from total_disposals,
             allocated_disposals,
             previous_disposals,
             unallocated_disposals
        """,
        nativeQuery = true
    )
    fun getProbationStatus(crn: String): ManagementStatus
}

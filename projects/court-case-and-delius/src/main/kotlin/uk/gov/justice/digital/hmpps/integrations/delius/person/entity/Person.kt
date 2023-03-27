package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

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
        with
            total_disposals as (select count(1) as cnt from (
                select o.crn, d.active_flag * e.active_flag as active_flag, s.officer_code
                from offender o
                join event e on e.offender_id = o.offender_id and e.soft_deleted = 0
                join order_manager om on om.event_id = e.event_id and om.active_flag = 1 and om.soft_deleted = 0
                join staff s on s.staff_id = om.allocation_staff_id
                join disposal d on d.event_id = e.event_id and d.soft_deleted = 0
                where crn = :crn
            )),
            unallocated_disposals as (select count(1) as cnt from (
                select o.crn, d.active_flag * e.active_flag as active_flag, s.officer_code
                from offender o
                join event e on e.offender_id = o.offender_id and e.soft_deleted = 0
                join order_manager om on om.event_id = e.event_id and om.active_flag = 1 and om.soft_deleted = 0
                join staff s on s.staff_id = om.allocation_staff_id
                join disposal d on d.event_id = e.event_id and d.soft_deleted = 0
                where crn = :crn
            ) where active_flag = 1 and officer_code like '%U'),
            allocated_disposals as (select count(1) as cnt from (
                select o.crn, d.active_flag * e.active_flag as active_flag, s.officer_code
                from offender o
                join event e on e.offender_id = o.offender_id and e.soft_deleted = 0
                join order_manager om on om.event_id = e.event_id and om.active_flag = 1 and om.soft_deleted = 0
                join staff s on s.staff_id = om.allocation_staff_id
                join disposal d on d.event_id = e.event_id and d.soft_deleted = 0
                where crn = :crn
            ) where active_flag = 1 and officer_code not like '%U'),
            previous_disposals as (select count(1) as cnt from (
                select o.crn, d.active_flag * e.active_flag as active_flag, s.officer_code
                from offender o
                join event e on e.offender_id = o.offender_id and e.soft_deleted = 0
                join order_manager om on om.event_id = e.event_id and om.active_flag = 1 and om.soft_deleted = 0
                join staff s on s.staff_id = om.allocation_staff_id
                join disposal d on d.event_id = e.event_id and d.soft_deleted = 0
                where crn = :crn
            ) where active_flag = 0)
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
    fun managedStatus(crn: String): String
}
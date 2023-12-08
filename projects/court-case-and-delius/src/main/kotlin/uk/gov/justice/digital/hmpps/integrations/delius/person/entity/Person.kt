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
    val id: Long,
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
        nativeQuery = true,
    )
    fun statusOf(crn: String): SentenceCounts?

    fun findByCrnAndSoftDeletedIsFalse(crn: String): Person?
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

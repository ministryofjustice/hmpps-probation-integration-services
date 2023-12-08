package uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "case_allocation")
class CaseAllocation(
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,
    @ManyToOne
    @JoinColumn(name = "allocation_decision_id")
    val decision: ReferenceData? = null,
    @Column(name = "allocation_decision_date")
    val decisionDate: ZonedDateTime? = null,
    @Id
    @Column(name = "case_allocation_id")
    val id: Long,
)

interface CaseAllocationRepository : JpaRepository<CaseAllocation, Long> {
    @Query(
        """
       select ca.decision from CaseAllocation ca
       join ca.decision
       join ca.event e
       join e.disposal d
       where ca.person.id = :personId and ca.decisionDate is not null
       and e.active = true and e.softDeleted = false
       and d.active = true and d.softDeleted = false
       order by ca.decisionDate desc
    """,
    )
    fun findLatestActiveDecision(
        personId: Long,
        pageRequest: PageRequest = PageRequest.of(0, 1),
    ): ReferenceData?
}

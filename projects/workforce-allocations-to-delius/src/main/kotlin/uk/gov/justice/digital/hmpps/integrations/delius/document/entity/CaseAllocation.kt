package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class CaseAllocation(
    @Id
    @Column(name = "case_allocation_id")
    val id: Long,

    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ManyToOne
    val event: DocEvent
)

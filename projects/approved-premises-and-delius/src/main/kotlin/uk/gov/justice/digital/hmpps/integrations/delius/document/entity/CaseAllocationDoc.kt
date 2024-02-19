package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "case_allocation")
class CaseAllocationDoc(
    @Id
    @Column(name = "case_allocation_id")
    val id: Long,

    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ManyToOne
    val event: DocEvent
)

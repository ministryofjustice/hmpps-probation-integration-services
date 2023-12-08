package uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "order_transfer")
class OrderTransfer(
    @Id
    @Column(name = "order_transfer_id")
    val id: Long,
    @Column(name = "event_id")
    val eventId: Long,
    @Column(name = "transfer_status_id")
    val statusId: Long,
    @Column(columnDefinition = "NUMBER", nullable = false)
    val softDeleted: Boolean,
)

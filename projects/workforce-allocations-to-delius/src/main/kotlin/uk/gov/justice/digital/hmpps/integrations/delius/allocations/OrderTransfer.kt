package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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

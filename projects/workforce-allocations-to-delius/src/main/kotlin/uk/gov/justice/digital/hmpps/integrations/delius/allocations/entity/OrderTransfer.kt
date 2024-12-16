package uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter

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

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

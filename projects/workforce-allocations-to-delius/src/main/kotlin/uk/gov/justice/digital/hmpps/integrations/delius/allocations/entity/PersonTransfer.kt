package uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter

@Immutable
@Entity
@Table(name = "offender_transfer")
class PersonTransfer(
    @Id
    @Column(name = "offender_transfer_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "transfer_status_id")
    val statusId: Long,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

package uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter

@Immutable
@Entity
@Table(name = "rqmnt_transfer")
class RequirementTransfer(
    @Id
    @Column(name = "rqmnt_transfer_id")
    val id: Long,

    @Column(name = "rqmnt_id")
    val requirementId: Long,

    @Column(name = "transfer_status_id")
    val statusId: Long,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

package uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter

@Immutable
@Entity
@Table(name = "lic_condition_transfer")
class LicenceConditionTransfer(
    @Id
    @Column(name = "lic_condition_transfer_id")
    val id: Long,

    @Column(name = "lic_condition_id")
    val licenceConditionId: Long,

    @Column(name = "transfer_status_id")
    val statusId: Long,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)
package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
@Table(name = "upw_adjustment")
class UnpaidWorkAdjustment(
    @Id
    @Column(name = "upw_adjustment_id")
    val id: Long,

    @Column(name = "upw_details_id")
    val upwDetailsId: Long,

    @Column(name = "adjustment_amount")
    val adjustmentAmount: Long,

    @Column(name = "adjustment_type")
    val adjustmentType: String,

    @Column(name = "soft_deleted")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
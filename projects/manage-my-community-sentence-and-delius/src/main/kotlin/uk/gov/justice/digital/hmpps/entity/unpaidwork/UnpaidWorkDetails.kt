package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Table(name = "upw_details")
@SQLRestriction("soft_deleted = 0")
class UnpaidWorkDetails(
    @Id
    @Column(name = "upw_details_id")
    val id: Long,
    val disposalId: Long,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
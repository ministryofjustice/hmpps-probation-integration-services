package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class Address(
    @Id
    @Column(name = "offender_address_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,

    @Column
    val buildingName: String?,

    @Column
    val addressNumber: String?,

    @Column
    val streetName: String?,

    @Column
    val townCity: String?,

    @Column
    val district: String?,

    @Column
    val county: String?,

    @Column
    val postcode: String?,

    @Convert(converter = YesNoConverter::class)
    val noFixedAbode: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

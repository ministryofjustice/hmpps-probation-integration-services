package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@Table(name = "address")
@SQLRestriction("soft_deleted = 0")
class Address(
    @Id
    @Column(name = "address_id")
    val id: Long,

    val buildingName: String? = null,

    @Column(name = "address_number")
    val buildingNumber: String? = null,

    val streetName: String? = null,

    @Column(name = "town_city")
    val townCity: String? = null,

    val district: String? = null,

    val county: String? = null,

    val postcode: String? = null,

    val telephoneNumber: String? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

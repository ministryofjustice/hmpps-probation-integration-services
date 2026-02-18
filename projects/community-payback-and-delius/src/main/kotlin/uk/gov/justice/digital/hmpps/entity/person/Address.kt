package uk.gov.justice.digital.hmpps.entity.person

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter

@Entity
@Table(name = "address")
@Immutable
class Address(
    @Id
    @Column(name = "address_id")
    val id: Long,

    val buildingName: String?,

    val addressNumber: String?,

    val streetName: String?,

    @Column(name = "town_city")
    val town: String?,

    val county: String?,

    val postcode: String?,

    val telephoneNumber: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
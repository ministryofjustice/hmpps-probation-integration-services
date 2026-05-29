package uk.gov.justice.digital.hmpps.entity.address

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
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

    val district: String?,

    val postcode: String?,

    val telephoneNumber: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)
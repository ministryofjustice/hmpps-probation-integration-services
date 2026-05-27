package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.address.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.address.PersonAddress
import java.time.LocalDateTime

data class Address(
    val houseNumber: String?,
    val buildingName: String?,
    val street: String?,
    val town: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
    val lastUpdatedAt: LocalDateTime? = null,
) {
    companion object {
        fun PersonAddress.toModel() = Address(
            houseNumber = addressNumber,
            buildingName = buildingName,
            street = streetName,
            town = town,
            district = district,
            county = county,
            postcode = postcode,
            lastUpdatedAt = lastUpdatedDatetime,
        )

        fun OfficeLocation.toModel() = Address(
            houseNumber = buildingNumber,
            buildingName = buildingName,
            street = streetName,
            town = town,
            district = district,
            county = county,
            postcode = postcode,
        )
    }
}
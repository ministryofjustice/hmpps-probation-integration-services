package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.address.OfficeLocation

data class OfficeAddress(
    val houseNumber: String?,
    val buildingName: String?,
    val street: String?,
    val town: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
) {
    companion object {
        fun OfficeLocation.toModel() = OfficeAddress(
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

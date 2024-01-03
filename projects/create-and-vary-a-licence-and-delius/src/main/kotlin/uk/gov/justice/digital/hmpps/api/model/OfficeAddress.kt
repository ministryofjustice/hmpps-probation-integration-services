package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class OfficeAddress(
    val officeName: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    val from: LocalDate,
    val to: LocalDate?
) {
    companion object {
        fun from(
            officeName: String,
            buildingName: String? = null,
            buildingNumber: String? = null,
            streetName: String? = null,
            district: String? = null,
            town: String? = null,
            county: String? = null,
            postcode: String? = null,
            telephoneNumber: String? = null,
            from: LocalDate,
            to: LocalDate? = null
        ): OfficeAddress? =
            if (
                buildingName == null && buildingNumber == null && streetName == null &&
                district == null && town == null && county == null && postcode == null
            ) {
                null
            } else {
                OfficeAddress(officeName, buildingName, buildingNumber, streetName, district, town, county, postcode, telephoneNumber, from, to)
            }
    }
}

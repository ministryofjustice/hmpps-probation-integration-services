package uk.gov.justice.digital.hmpps.api.model.personalDetails

import java.time.LocalDate

data class Address(
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val from: LocalDate,
    val to: LocalDate?,
    val lastUpdated: LocalDate?,
    val type: String?,
    val status: String?
) {
    companion object {
        fun from(
            buildingName: String? = null,
            buildingNumber: String? = null,
            streetName: String? = null,
            district: String? = null,
            town: String? = null,
            county: String? = null,
            postcode: String? = null,
            from: LocalDate,
            to: LocalDate? = null,
            lastUpdated: LocalDate? = null,
            type: String? = null,
            status: String? = null
        ): Address? =
            if (
                buildingName == null && buildingNumber == null && streetName == null &&
                district == null && town == null && county == null && postcode == null
            ) {
                null
            } else {
                Address(buildingName, buildingNumber, streetName, district, town, county, postcode, from, to, lastUpdated, type, status)
            }
    }
}
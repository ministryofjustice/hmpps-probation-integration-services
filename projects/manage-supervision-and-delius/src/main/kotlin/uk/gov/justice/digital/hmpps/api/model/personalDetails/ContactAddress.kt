package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class ContactAddress(
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val lastUpdated: LocalDate,
    val lastUpdatedBy: Name,
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
            lastUpdated: LocalDate,
            lastUpdatedBy: Name
        ): ContactAddress? =
            if (
                buildingName == null && buildingNumber == null && streetName == null &&
                district == null && town == null && county == null && postcode == null
            ) {
                null
            } else {
                ContactAddress(
                    buildingName,
                    buildingNumber,
                    streetName,
                    district,
                    town,
                    county,
                    postcode,
                    lastUpdated,
                    lastUpdatedBy
                )
            }
    }
}
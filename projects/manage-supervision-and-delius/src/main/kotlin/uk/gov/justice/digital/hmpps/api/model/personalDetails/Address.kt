package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class Address(
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    val from: LocalDate,
    val to: LocalDate?,
    val verified: Boolean?,
    val lastUpdated: LocalDate?,
    val lastUpdatedBy: Name,
    val type: String?,
    val status: String?,
    val notes: String?
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
            telephoneNumber: String?,
            from: LocalDate,
            to: LocalDate? = null,
            verified: Boolean? = null,
            lastUpdated: LocalDate? = null,
            lastUpdatedBy: Name,
            type: String? = null,
            status: String? = null,
            notes: String? = null
        ): Address? =
            if (
                buildingName == null && buildingNumber == null && streetName == null &&
                district == null && town == null && county == null && postcode == null
            ) {
                null
            } else {
                Address(
                    buildingName,
                    buildingNumber,
                    streetName,
                    district,
                    town,
                    county,
                    postcode,
                    telephoneNumber,
                    from,
                    to,
                    verified,
                    lastUpdated,
                    lastUpdatedBy,
                    type,
                    status,
                    notes
                )
            }
    }
}
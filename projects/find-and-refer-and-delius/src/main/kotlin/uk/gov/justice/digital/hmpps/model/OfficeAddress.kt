package uk.gov.justice.digital.hmpps.model

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
    val email: String?,
    val ldu: String,
    val telephoneNumber: String?,
    val from: LocalDate,
    val to: LocalDate?
) {
    companion object {
        fun from(
            officeName: String,
            buildingName: String?,
            buildingNumber: String?,
            streetName: String?,
            district: String?,
            town: String?,
            county: String?,
            postcode: String?,
            email: String?,
            ldu: String,
            telephoneNumber: String?,
            from: LocalDate,
            to: LocalDate?
        ): OfficeAddress? =
            if (
                buildingName == null && buildingNumber == null && streetName == null &&
                district == null && town == null && county == null && postcode == null
            ) {
                null
            } else {
                OfficeAddress(
                    officeName,
                    buildingName,
                    buildingNumber,
                    streetName,
                    district,
                    town,
                    county,
                    postcode,
                    email,
                    ldu,
                    telephoneNumber,
                    from,
                    to
                )
            }
    }
}

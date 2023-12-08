package uk.gov.justice.digital.hmpps.api.model

data class OfficeLocation(
    val code: String,
    val description: String,
    val address: Address?,
    val telephoneNumber: String?,
)

data class Address(
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
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
        ): Address? =
            if (
                buildingName == null && buildingNumber == null && streetName == null &&
                district == null && town == null && county == null && postcode == null
            ) {
                null
            } else {
                Address(buildingName, buildingNumber, streetName, district, town, county, postcode)
            }
    }
}

package uk.gov.justice.digital.hmpps.api.model.schedule

data class OfficeAddress(
    val officeName: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val ldu: String,
    val telephoneNumber: String?
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
            ldu: String,
            telephoneNumber: String?
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
                    ldu,
                    telephoneNumber,
                )
            }
    }
}
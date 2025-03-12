package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.OfficeLocation

object OfficeLocationGenerator {
    val DEFAULT_LOCATION = generateLocation(
        description = "Default Office",
        buildingNumber = "21",
        streetName = "Mantle Place",
        district = "Nr Fire",
        town = "Hearth",
        postcode = "H34 7TH"
    )

    fun generateLocation(
        description: String,
        buildingName: String? = null,
        buildingNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = OfficeLocation(
        description,
        buildingName,
        buildingNumber,
        streetName,
        district,
        town,
        county,
        postcode,
        null,
        ProviderGenerator.DEFAULT_PROVIDER,
        id
    )
}
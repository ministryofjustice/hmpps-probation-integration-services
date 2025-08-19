package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.OfficeLocation

object OfficeLocationGenerator {
    val DEFAULT = OfficeLocation(
        id = 1,
        description = "Default Office",
        buildingName = null,
        buildingNumber = "123",
        streetName = "Default Street",
        district = null,
        townCity = "Default Town",
        county = "Default County",
        postcode = "AB1 2CD",
        endDate = null,
        provider = ProviderGenerator.N00
    )
}
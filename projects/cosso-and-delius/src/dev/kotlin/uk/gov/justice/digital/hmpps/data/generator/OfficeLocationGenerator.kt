package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.OfficeLocation

object OfficeLocationGenerator {
    val DEFAULT_OFFICE_LOCATION = OfficeLocation(
        id = 10001099,
        description = "Main Office",
        buildingName = "The Office Block",
        buildingNumber = "1",
        streetName = "The Street",
        townCity = "The Town",
        county = "The County",
        district = "The District",
        postcode = "AA1 1AA",
        endDate = null
    )
}
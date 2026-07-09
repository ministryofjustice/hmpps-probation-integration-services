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
        endDate = null,
        provider = ProbationAreaGenerator.PROBATION_AREA_N01,
    )

    val OFFICE_LOCATION_2 = OfficeLocation(
        id = 10001100,
        description = "Office 2",
        buildingName = "Office Block 2",
        buildingNumber = "2",
        streetName = "Street 2",
        townCity = "Town 2",
        county = "County 2",
        district = "District 2",
        postcode = "AA1 1AB",
        endDate = null,
        provider = ProbationAreaGenerator.PROBATION_AREA_N01,
    )
}
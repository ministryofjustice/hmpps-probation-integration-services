package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.OfficeLocation

object OfficeLocationGenerator {
    const val DEFAULT_OFFICE_LOCATION_ID = 1000010L

    val DEFAULT_OFFICE_LOCATION = OfficeLocation(
        id = DEFAULT_OFFICE_LOCATION_ID,
        description = "Jail Centre Plus",
        buildingName = null,
        buildingNumber = "281",
        streetName = "Postal Default Street",
        townCity = "Postinton",
        district = "Postrict",
        county = "County Post",
        postcode = "NE30 3ZZ",
        endDate = null,
        probationArea = ProbationAreaGenerator.HOME_PROBATION_AREA,
    )
}

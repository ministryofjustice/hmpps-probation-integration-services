package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator.DEFAULT_DISTRICT
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.entity.District
import uk.gov.justice.digital.hmpps.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.TeamOfficeLink
import uk.gov.justice.digital.hmpps.entity.TeamOfficeLinkId
import java.time.LocalDate

object OfficeLocationGenerator {

    val LOCATION_1 = generateOfficeLocation(
        code = "L1",
        description = "Location 1",
        buildingNumber = "1",
        buildingName = "The building",
        streetName = "The Street",
        townCity = "The Town",
        county = "The County",
        postCode = "P1 1PC",
        ldu = DEFAULT_DISTRICT,
        district = DEFAULT_DISTRICT.description,
        telephone = "01234567890"
    )

    val LOCATION_2 = generateOfficeLocation(
        code = "L1",
        description = "Location 2",
        buildingNumber = "2",
        buildingName = "Another building",
        streetName = "Another Street",
        townCity = "The Town",
        county = "The County",
        postCode = "P1 APC",
        ldu = DEFAULT_DISTRICT,
        district = DEFAULT_DISTRICT.description,
        telephone = "01234567123"
    )

    val TEAM_OFFICE_1 = TeamOfficeLink(TeamOfficeLinkId(DEFAULT_TEAM.id, LOCATION_1))
    val TEAM_OFFICE_2 = TeamOfficeLink(TeamOfficeLinkId(DEFAULT_TEAM.id, LOCATION_2))

    private fun generateOfficeLocation(
        code: String,
        description: String,
        buildingNumber: String? = null,
        buildingName: String? = null,
        streetName: String? = null,
        townCity: String? = null,
        county: String? = null,
        postCode: String? = null,
        ldu: District,
        district: String? = null,
        telephone: String? = null,
        startDate: LocalDate = LocalDate.now().minusDays(10),
        endDate: LocalDate? = null
    ) =
        OfficeLocation(
            id = IdGenerator.getAndIncrement(),
            code = code,
            description = description,
            buildingNumber = buildingNumber,
            buildingName = buildingName,
            streetName = streetName,
            district = district,
            townCity = townCity,
            postcode = postCode,
            county = county,
            ldu = ldu,
            telephoneNumber = telephone,
            startDate = startDate,
            endDate = endDate
        )
}

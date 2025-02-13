package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.OfficeLocation
import uk.gov.justice.digital.hmpps.integrations.delius.Team
import java.time.LocalDate

object TeamGenerator {
    val DEFAULT_LOCATION = generateLocation(
        code = "DEF_OFF",
        description = "Default Office",
        buildingNumber = "21",
        streetName = "Mantle Place",
        district = "Nr Fire",
        town = "Hearth",
        postcode = "H34 7TH"
    )
    val DEFAULT_TEAM = generateTeam("D37TEM", addresses = setOf(DEFAULT_LOCATION))

    fun generateTeam(
        code: String,
        description: String = "Description of $code",
        telephone: String? = "1234567890",
        emailAddress: String? = "test@email.com",
        addresses: Set<OfficeLocation> = setOf(),
        startDate: LocalDate = LocalDate.now().minusMonths(1),
        endDate: LocalDate? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(
        code,
        description,
        telephone,
        emailAddress,
        addresses,
        startDate,
        endDate,
        id
    )

    fun generateLocation(
        code: String,
        description: String,
        buildingName: String? = null,
        buildingNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = OfficeLocation(
        code,
        description,
        buildingName,
        buildingNumber,
        streetName,
        district,
        town,
        county,
        postcode,
        startDate,
        endDate,
        id
    )
}
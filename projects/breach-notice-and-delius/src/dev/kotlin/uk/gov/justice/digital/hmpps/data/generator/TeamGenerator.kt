package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.OfficeLocation
import uk.gov.justice.digital.hmpps.integrations.delius.Team
import java.time.LocalDate

object TeamGenerator {
    val DEFAULT_LOCATION = generateLocation(
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
        addresses: Set<OfficeLocation> = setOf(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(
        code,
        description,
        addresses,
        null,
        id
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
        id
    )
}
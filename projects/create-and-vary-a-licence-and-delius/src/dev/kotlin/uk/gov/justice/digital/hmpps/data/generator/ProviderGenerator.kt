package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import java.time.LocalDate

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider("N01")
    val DEFAULT_BOROUGH = generateBorough("N01B")
    val DEFAULT_DISTRICT = generateDistrict("N01D")
    val DEFAULT_TEAM = generateTeam("N01BDT")
    val TEAM_ENDED_LOCATIONS = generateTeam(
        addresses = listOf(
            OfficeLocationGenerator.LOCATION_BRK_1,
            OfficeLocationGenerator.LOCATION_BRK_2,
            OfficeLocationGenerator.LOCATION_ENDED
        ), code = "N01BDT"
    )

    fun generateProvider(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement(),
        endDate: LocalDate? = null
    ) = Provider(code, description, id, endDate)

    fun generateBorough(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement(),
        pduHeads: List<Staff> = listOf(StaffGenerator.PDUHEAD)
    ) = Borough(code, description, id, pduHeads, DEFAULT_PROVIDER)

    fun generateDistrict(
        code: String,
        description: String = "Description of $code",
        borough: Borough = DEFAULT_BOROUGH,
        id: Long = IdGenerator.getAndIncrement()
    ) = District(code, description, borough, id)

    fun generateTeam(
        code: String,
        description: String = "Description of $code",
        telephone: String? = "12345",
        emailAddress: String? = "testemail",
        district: District = DEFAULT_DISTRICT,
        addresses: List<OfficeLocation> = listOf(
            OfficeLocationGenerator.LOCATION_BRK_1,
            OfficeLocationGenerator.LOCATION_BRK_2
        ),
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, telephone, emailAddress, district, addresses, startDate, endDate, id)
}

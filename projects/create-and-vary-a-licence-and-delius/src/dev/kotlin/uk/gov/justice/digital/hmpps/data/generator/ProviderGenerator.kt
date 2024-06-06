package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.OfficeAddress
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import java.time.LocalDate

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider("N01")
    val DEFAULT_BOROUGH = generateBorough("N01B")
    val DEFAULT_DISTRICT = generateDistrict("N01D")
    val DISTRICT_BRK = generateDistrict("TVP_BRK", "Berkshire")
    val DISTRICT_OXF = generateDistrict("TVP_OXF", "Oxfordshire")
    val DISTRICT_MKY = generateDistrict("TVP_MKY", "Milton Keynes")

    val LOCATION_BRK_1 = generateLocation(
        code = "TVP_BRK",
        description = "Bracknell Office",
        buildingNumber = "21",
        streetName = "Some Place",
        district = "District 1",
        town = "Hearth",
        postcode = "H34 7TH",
        ldu = DISTRICT_BRK
    )

    val LOCATION_BRK_2 = generateLocation(
        code = "TVP_RCC",
        description = "Reading Office",
        buildingNumber = "23",
        buildingName = "The old hall",
        streetName = "Another Place",
        district = "District 2",
        town = "Reading",
        postcode = "RG1 3EH",
        ldu = DISTRICT_BRK
    )
    val LOCATION_ENDED = generateLocation(
        code = "TVP_RCC",
        description = "Reading Office",
        buildingNumber = "23",
        buildingName = "The old hall",
        streetName = "Another Place",
        district = "District 2",
        town = "Reading",
        postcode = "RG1 3EH",
        endDate = LocalDate.now().minusDays(1),
        ldu = DISTRICT_BRK
    )

    val LOCATION_NULL = generateLocation(
        code = "TVP_RCC",
        description = "Null office",
        ldu = DEFAULT_DISTRICT
    )

    val DEFAULT_TEAM = generateTeam("N01BDT")

    val TEAM_ENDED_OR_NULL_LOCATIONS = generateTeam(
        addresses = listOf(
            LOCATION_BRK_1,
            LOCATION_BRK_2,
            LOCATION_ENDED,
            LOCATION_NULL
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
            LOCATION_BRK_1,
            LOCATION_BRK_2
        ),
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, telephone, emailAddress, district, addresses, startDate, endDate, id)

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
        telephoneNumber: String? = null,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        ldu: District,
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
        telephoneNumber,
        startDate,
        endDate,
        ldu,
        id
    )
}

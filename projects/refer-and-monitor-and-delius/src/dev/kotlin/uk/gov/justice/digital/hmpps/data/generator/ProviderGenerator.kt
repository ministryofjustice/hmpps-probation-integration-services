package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Location
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate

object ProviderGenerator {
    val INTENDED_PROVIDER = generateProvider(Provider.INTENDED_PROVIDER_CODE, "Test Provider")
    val NON_CRS_PROVIDER = generateProvider("N01", "Non-CRS Provider")
    val INACTIVE_PROVIDER = generateProvider("N02", "Inactive Provider", endDate = LocalDate.of(2020, 1, 1))
    val PROBATION_BOROUGH = generateBorough("PDU01")
    val PRISON_BOROUGH = generateBorough("PDU02")
    val PROBATION_DISTRICT = generateDistrict("LDU01", borough = PROBATION_BOROUGH)
    val PRISON_DISTRICT = generateDistrict("LDU02", borough = PRISON_BOROUGH)
    val INTENDED_TEAM = generateTeam(Team.INTENDED_TEAM_CODE)
    val INTENDED_STAFF = generateStaff(Staff.INTENDED_STAFF_CODE, "Intended", "Staff")
    val DEFAULT_LOCATION = generateLocation("DEFAULT")
    val PROBATION_TEAM = generateTeam("N01PRO")
    val PRISON_TEAM = generateTeam("P01PRI", district = PRISON_DISTRICT)
    val JOHN_SMITH = generateStaff("N01RMT1", "John", "Smith")
    val JOHN_SMITH_USER = generateStaffUser("john-smith", JOHN_SMITH)
    val PRISON_MANAGER = generateStaff("P01WDN1", "Peter", "Wilson")

    val LOCATIONS = listOf(
        generateLocation("TESTONE", buildingName = "Test One", streetName = "Mantle Place", postcode = "MP1 1PM"),
        generateLocation("TESTTWO", buildingName = "Test Two", postcode = "MP2 2PM", telephoneNumber = "020 123 6789"),
        generateLocation("NOTCRS", provider = NON_CRS_PROVIDER),
        generateLocation("DEFAULT", provider = INACTIVE_PROVIDER), // duplicate code linked to inactive provider
        generateLocation("ENDDATE", endDate = LocalDate.of(2020, 1, 1))
    )

    fun generateProvider(
        code: String,
        description: String,
        endDate: LocalDate? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Provider(code, description, endDate, id)

    fun generateBorough(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Borough(code, description, id)

    fun generateDistrict(
        code: String,
        description: String = "Description of $code",
        borough: Borough,
        id: Long = IdGenerator.getAndIncrement()
    ) = District(code, description, borough, id)

    fun generateTeam(
        code: String,
        description: String = "Team $code",
        district: District = PROBATION_DISTRICT,
        email: String? = "team@$code.co.uk",
        telephone: String? = "020 785 4451",
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, district, email, telephone, id)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        user: StaffUser? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, user, id)

    fun generateStaffUser(
        username: String,
        staff: Staff? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(username, staff, id).apply { staff?.set(Staff::user, this) }

    fun generateLocation(
        code: String,
        description: String = "Description of $code",
        buildingName: String? = null,
        buildingNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        townCity: String? = null,
        county: String? = null,
        postcode: String? = null,
        telephoneNumber: String? = null,
        startDate: LocalDate = LocalDate.now().minusDays(7),
        endDate: LocalDate? = null,
        provider: Provider = INTENDED_PROVIDER,
        id: Long = IdGenerator.getAndIncrement()
    ) = Location(
        code,
        description,
        buildingName,
        buildingNumber,
        streetName,
        district,
        townCity,
        county,
        postcode,
        telephoneNumber,
        startDate,
        endDate,
        provider,
        id
    )
}

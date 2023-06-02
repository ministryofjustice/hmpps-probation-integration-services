package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Location
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.set

object ProviderGenerator {
    val INTENDED_PROVIDER = generateProvider(Provider.INTENDED_PROVIDER_CODE, "Test Provider")
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

    fun generateProvider(code: String, description: String, id: Long = IdGenerator.getAndIncrement()) = Provider(code,description, id)

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

    fun generateTeam(code: String, district: District = PROBATION_DISTRICT, id: Long = IdGenerator.getAndIncrement()) =
        Team(code, district, id)

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

    fun generateLocation(code: String, id: Long = IdGenerator.getAndIncrement()) = Location(code, id)
}

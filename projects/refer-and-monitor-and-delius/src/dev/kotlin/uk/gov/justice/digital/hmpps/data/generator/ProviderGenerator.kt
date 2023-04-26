package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Location
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.set

object ProviderGenerator {
    val INTENDED_PROVIDER = generateProvider(Provider.INTENDED_PROVIDER_CODE)
    val INTENDED_TEAM = generateTeam(Team.INTENDED_TEAM_CODE)
    val INTENDED_STAFF = generateStaff(Staff.INTENDED_STAFF_CODE, "Intended", "Staff")
    val DEFAULT_LOCATION = generateLocation("DEFAULT")
    val JOHN_SMITH = generateStaff("N01RMT1", "John", "Smith")
    val JOHN_SMITH_USER = generateStaffUser("john-smith", JOHN_SMITH)
    val PRISON_MANAGER = generateStaff("P01WDN1", "Peter", "Wilson")

    fun generateProvider(code: String, id: Long = IdGenerator.getAndIncrement()) = Provider(code, id)
    fun generateTeam(code: String, id: Long = IdGenerator.getAndIncrement()) = Team(code, id)
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

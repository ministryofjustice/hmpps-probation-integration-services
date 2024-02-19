package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.set

object StaffGenerator {
    val PDUHEAD = generateStaff("N01BDT2", "Bob", "Smith")
    val DEFAULT_PDUSTAFF_USER = generateStaffUser("bob-smith", PDUHEAD)
    var DEFAULT = generateStaff("N01BDT1", "John", "Smith", teams = listOf(ProviderGenerator.DEFAULT_TEAM))
    val DEFAULT_STAFF_USER = generateStaffUser("john-smith", DEFAULT)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        teams: List<Team> = listOf(),
        middleName: String? = null,
        user: StaffUser? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, middleName, user, id, teams).apply { user?.set("staff", this) }

    fun generateStaffUser(
        username: String,
        staff: Staff? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(staff, username, id).apply { staff?.set("user", this) }
}

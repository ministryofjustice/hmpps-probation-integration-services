package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_BOROUGH
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.set

object StaffGenerator {
    val PDUHEAD = generateStaff("N01BDT2", "Bob", "Smith").also { DEFAULT_BOROUGH.set(Borough::pduHeads, listOf(it)) }
    val DEFAULT_PDUSTAFF_USER = generateStaffUser("bob-smith", PDUHEAD)
    var DEFAULT = generateStaff("N01BDT1", "John", "Smith", teams = listOf(ProviderGenerator.DEFAULT_TEAM))
    val DEFAULT_STAFF_USER = generateStaffUser("john-smith", DEFAULT)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        teams: List<Team> = listOf(),
        provider: Provider = DEFAULT_PROVIDER,
        middleName: String? = null,
        user: StaffUser? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, middleName, user, id, teams, provider).apply { user?.set("staff", this) }

    fun generateStaffUser(
        username: String,
        staff: Staff? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(staff, username, id).apply { staff?.set("user", this) }
}

package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.integrations.delius.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.Team

object StaffGenerator {
    val DEFAULT_STAFF = generateStaff("D37TEMA", "John", "Smith", teams = setOf(DEFAULT_TEAM))
    val DEFAULT_SU = generateStaffUser("J0nSm17h", DEFAULT_STAFF)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        middleName: String? = null,
        teams: Set<Team> = setOf(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, middleName, teams, id)

    fun generateStaffUser(
        username: String,
        staff: Staff? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(staff, username, id)
}
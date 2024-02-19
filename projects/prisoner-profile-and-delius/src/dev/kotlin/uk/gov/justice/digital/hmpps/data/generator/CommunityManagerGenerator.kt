package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.CommunityManager
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.ManagedPerson
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.Team

object CommunityManagerGenerator {
    val TEAM = generateTeam("N01PPT", telephone = "020 039 6743")
    val UNALLOCATED_STAFF = generateStaff("N01PPTU", "Unallocated", "Staff")
    val STAFF = generateStaff("N01PPTA", "James", "Brown")
    val JAMES_BROWN = generateStaffUser("james-brown", STAFF)
    val ALLOCATED_PERSON = generatePerson("A123456", "A1234TD")
    val UNALLOCATED_PERSON = generatePerson("U123456", "U1234TD")

    fun generateTeam(
        code: String,
        description: String = "Description of $code",
        email: String? = "$code@justice.gov.uk",
        telephone: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, email, telephone, id)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        user: StaffUser? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, user, id)

    fun generateStaffUser(
        username: String,
        staff: Staff,
        id: Long = IdGenerator.getAndIncrement()
    ) = StaffUser(username, staff, id)

    fun generatePerson(
        crn: String,
        nomsId: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = ManagedPerson(crn, nomsId, softDeleted, id)

    fun generateCommunityManager(
        person: ManagedPerson,
        staff: Staff,
        team: Team = TEAM,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = CommunityManager(person, team, staff, active, softDeleted, id)
}

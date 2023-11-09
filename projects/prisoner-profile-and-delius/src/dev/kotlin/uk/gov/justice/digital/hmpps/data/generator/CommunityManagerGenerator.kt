package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.CommunityManager
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.Team

object CommunityManagerGenerator {
    val TEAM = generateTeam("N01PPT")
    val UNALLOCATED_STAFF = generateStaff("N01PPTU", "Unallocated", "Staff")
    val STAFF = generateStaff("N01PPTA", "James", "Brown")
    val ALLOCATED_PERSON = generatePerson("A123456", "A1234TD")
    val UNALLOCATED_PERSON = generatePerson("U123456", "U1234TD")

    fun generateTeam(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, id)

    fun generateStaff(code: String, forename: String, surname: String, id: Long = IdGenerator.getAndIncrement()) =
        Staff(code, forename, surname, id)

    fun generatePerson(
        crn: String,
        nomsId: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, nomsId, softDeleted, id)

    fun generateCommunityManager(
        person: Person,
        staff: Staff,
        team: Team = TEAM,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = CommunityManager(person, team, staff, active, softDeleted, id)
}

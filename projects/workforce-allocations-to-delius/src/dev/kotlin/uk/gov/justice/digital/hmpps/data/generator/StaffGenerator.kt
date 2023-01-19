package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRecord
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffUser
import uk.gov.justice.digital.hmpps.set

object StaffGenerator {
    val DEFAULT = generateStaff(
        "${TeamGenerator.DEFAULT.code}U", "Unallocated", "Staff", listOf(TeamGenerator.DEFAULT)
    )
    val STAFF_FOR_INACTIVE_EVENT = generateStaff(
        "INACTI1",
        "John",
        "Smith"
    )
    val STAFF_WITH_USER = generateStaffWithUser(
        "${TeamGenerator.ALLOCATION_TEAM.code}1",
        "Joe",
        "Bloggs",
        listOf(TeamGenerator.ALLOCATION_TEAM),
        StaffUserGenerator.DEFAULT
    )

    fun generateStaffWithUser(
        code: String,
        forename: String = "Test",
        surname: String = "Test",
        teams: List<Team> = listOf(),
        user: StaffUser? = StaffUserGenerator.generate(code),
        grade: ReferenceData = ReferenceDataGenerator.PSQ_GRADE,
        id: Long = IdGenerator.getAndIncrement(),
    ): StaffWithUser = generate(code, forename, surname, grade, teams, id, user, true) as StaffWithUser

    fun generateStaff(
        code: String,
        forename: String = "Test",
        surname: String = "Test",
        teams: List<Team> = listOf(),
        grade: ReferenceData = ReferenceDataGenerator.PSQ_GRADE,
        id: Long = IdGenerator.getAndIncrement(),
    ): Staff = generate(code, forename, surname, grade, teams, id) as Staff

    fun generate(
        code: String,
        forename: String,
        surname: String,
        grade: ReferenceData = ReferenceDataGenerator.PSQ_GRADE,
        teams: List<Team> = listOf(),
        id: Long = IdGenerator.getAndIncrement(),
        user: StaffUser? = null,
        withUser: Boolean = false
    ): StaffRecord =
        if (withUser) {
            val staff = StaffWithUser(
                id = id,
                code = code,
                forename = forename,
                surname = surname,
                grade = grade,
                user = user,
                teams = teams,
            )
            user?.set("staff", staff)
            staff
        } else {
            Staff(id, code, forename, surname, grade = grade, teams = teams)
        }
}

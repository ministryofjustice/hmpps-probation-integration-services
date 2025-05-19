package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.provider.*
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffWithTeamUser
import uk.gov.justice.digital.hmpps.set
import java.time.ZonedDateTime

object StaffGenerator {
    val DEFAULT = generateStaff(
        "${TeamGenerator.DEFAULT.code}U",
        "Unallocated",
        "Staff",
        listOf(TeamGenerator.DEFAULT)
    )
    val STAFF_FOR_INACTIVE_EVENT = generateStaff(
        "INACTI1",
        "John",
        "Smith"
    )
    val ALLOCATED = generateStaff(
        "TEST01",
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
    val INACTIVE_STAFF = generateStaffWithUser(
        "${TeamGenerator.ALLOCATION_TEAM.code}2",
        "Joe",
        "Bloggs",
        listOf(TeamGenerator.ALLOCATION_TEAM),
        StaffUserGenerator.generate("inactive"),
        endDate = ZonedDateTime.now().minusDays(7)
    )

    val SPO_STAFF = generateStaffWithUser(
        "${TeamGenerator.ALLOCATION_TEAM.code}X",
        "Joe",
        "Bloggs",
        listOf(TeamGenerator.ALLOCATION_TEAM)
    )

    val STAFF_WITH_TEAM = StaffWithTeams(
        id = IdGenerator.getAndIncrement(),
        code = "SWT0001",
        teams = listOf(TeamGenerator.TEAM_IN_LAU),
    )

    val STAFF_WITH_TEAM_AND_USER = StaffWithTeamUser(
        id = IdGenerator.getAndIncrement(),
        username = "s001wt",
        staff = STAFF_WITH_TEAM,
        datasets = listOf(ProviderGenerator.DEFAULT),
    )

    fun generateStaffWithUser(
        code: String,
        forename: String = "Test",
        surname: String = "Test",
        teams: List<Team> = listOf(),
        user: StaffUser? = StaffUserGenerator.generate(code),
        grade: ReferenceData = ReferenceDataGenerator.PSQ_GRADE,
        endDate: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ): StaffWithUser = generate(code, forename, surname, grade, teams, id, user, endDate, true) as StaffWithUser

    fun generateStaff(
        code: String,
        forename: String = "Test",
        surname: String = "Test",
        teams: List<Team> = listOf(),
        grade: ReferenceData = ReferenceDataGenerator.PSQ_GRADE,
        id: Long = IdGenerator.getAndIncrement()
    ): Staff = generate(code, forename, surname, grade, teams, id) as Staff

    fun generate(
        code: String,
        forename: String,
        surname: String,
        grade: ReferenceData = ReferenceDataGenerator.PSQ_GRADE,
        teams: List<Team> = listOf(),
        id: Long = IdGenerator.getAndIncrement(),
        user: StaffUser? = null,
        endDate: ZonedDateTime? = null,
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
                endDate = endDate
            )
            user?.set("staff", staff)
            staff
        } else {
            Staff(id, code, forename, surname, grade = grade, teams = teams)
        }
}

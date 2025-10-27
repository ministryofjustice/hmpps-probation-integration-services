package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team
import java.time.LocalDate

object StaffGenerator {
    val DEFAULT_STAFF = generateStaff(
        code = "N01P001",
        forename = "Default",
        surname = "Staff",
        teams = listOf(TeamGenerator.DEFAULT_UPW_TEAM)
    )

    val SECOND_STAFF = generateStaff(
        code = "N01P002",
        forename = "Second",
        surname = "Staff",
        teams = listOf(TeamGenerator.DEFAULT_UPW_TEAM)
    )

    fun generateStaff(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        forename: String,
        surname: String,
        middleName: String? = null,
        grade: ReferenceData? = null,
        startDate: LocalDate = LocalDate.now().minusDays(1),
        endDate: LocalDate? = null,
        teams: List<Team> = emptyList()
    ) = Staff(id, code, forename, surname, middleName, grade, startDate, endDate, teams)
}
package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.entity.staff.User
import java.time.LocalDate

object StaffGenerator {
    fun generate(
        code: String,
        user: User? = null,
        teams: List<Team> = listOf(),
        endDate: LocalDate? = null,
        middleName: String? = null,
    ) = Staff(
        id = id(),
        code = code,
        forename = "Forename",
        middleName = middleName,
        surname = "Surname",
        user = user,
        teams = teams,
        endDate = endDate
    )
}

package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.entity.staff.User

object StaffGenerator {
    fun generate(code: String = "STAFF01", user: User? = null, teams: List<Team> = listOf()) = Staff(
        id = id(),
        code = code,
        forename = "Forename",
        surname = "Surname",
        user = user,
        teams = teams
    )
}

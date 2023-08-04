package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ProbationArea
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.Team

object ProviderGenerator {
    val DEFAULT_AREA = generateProbationArea()
    val DEFAULT_TEAM = generateTeam("N03DEF")
    val DEFAULT_STAFF = generateStaff("Default", "Middlename", "Staff")

    fun generateProbationArea(
        id: Long = IdGenerator.getAndIncrement(),
        code: String = "LDN",
        description: String = "London"
    ) = ProbationArea(id, code, description)
    fun generateTeam(
        code: String,
        description: String = "Team $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(id, code, description)

    fun generateStaff(
        forename: String,
        surname: String,
        middleName: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(id, forename, middleName, surname)
}

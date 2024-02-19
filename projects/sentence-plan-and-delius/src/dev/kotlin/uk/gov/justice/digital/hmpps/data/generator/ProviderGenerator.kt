package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.service.entity.ProbationArea
import uk.gov.justice.digital.hmpps.service.entity.Staff
import uk.gov.justice.digital.hmpps.service.entity.Team

object ProviderGenerator {
    val DEFAULT_AREA = generateProbationArea()
    val DEFAULT_TEAM = generateTeam("N03DEF", probationArea = DEFAULT_AREA)
    val DEFAULT_STAFF = generateStaff("N03DEFU", "Default", "Staff", "Middlename")

    fun generateProbationArea(
        id: Long = IdGenerator.getAndIncrement(),
        description: String = "London"
    ) = ProbationArea(id, description)

    fun generateTeam(
        code: String,
        description: String = "Team $code",
        id: Long = IdGenerator.getAndIncrement(),
        probationArea: ProbationArea
    ) = Team(code, description, probationArea, id)

    fun generateStaff(
        code: String,
        forename: String,
        surname: String,
        middleName: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(code, forename, surname, middleName, id)
}

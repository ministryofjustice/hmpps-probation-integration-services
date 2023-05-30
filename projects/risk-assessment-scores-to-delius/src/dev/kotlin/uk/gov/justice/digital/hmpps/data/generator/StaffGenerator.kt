package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team

object StaffGenerator {

    val DEFAULT = generate()
    fun generate(
        name: String = "TEST",
        code: String = "TEST",
        teams: List<Team> = listOf()
    ) = Staff(
        id = IdGenerator.getAndIncrement(),
        code = code,
        forename = "Test",
        middleName = null,
        surname = name,
        teams = teams
    )
}

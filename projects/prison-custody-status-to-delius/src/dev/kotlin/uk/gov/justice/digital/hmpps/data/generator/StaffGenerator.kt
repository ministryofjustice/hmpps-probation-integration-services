package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team

object StaffGenerator {
    var DEFAULT = generate(
        "${ProbationAreaGenerator.DEFAULT.code}A001",
        "Joe",
        "Bloggs"
    )

    var UNALLOCATED = unallocated(TeamGenerator.DEFAULT)

    fun unallocated(team: Team) = generate("${team.code}U", "Unallocated", "Staff", listOf(team))

    fun generate(
        code: String,
        forename: String,
        surname: String,
        teams: List<Team> = listOf(TeamGenerator.DEFAULT),
        id: Long = IdGenerator.getAndIncrement()
    ) = Staff(id, code, forename, null, surname, teams)
}

package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.Team

object StaffGenerator {
    val DEFAULT = Staff(
        id = IdGenerator.getAndIncrement(),
        code = "N00A001",
        forename = "Probation",
        middleName = "PO",
        surname = "Officer",
        teams = listOf(TeamGenerator.DEFAULT),
    )
    val TEAM_STAFF = Staff(
        id = IdGenerator.getAndIncrement(),
        code = "N00A002",
        forename = "Another",
        surname = "Officer",
        teams = listOf(TeamGenerator.DEFAULT),
    )
    val OTHER_TEAM_STAFF = Staff(
        id = IdGenerator.getAndIncrement(),
        code = "N00A020",
        forename = "Other",
        middleName = "Team",
        surname = "Officer",
        teams = listOf(TeamGenerator.OTHER_TEAM),
    )
}

object TeamGenerator {
    val DEFAULT = Team(
        id = IdGenerator.getAndIncrement(),
        code = "N00T01",
        description = "Team 1",
    )
    val OTHER_TEAM = Team(
        id = IdGenerator.getAndIncrement(),
        code = "N00T02",
        description = "Team 2",
    )
}

object ProviderGenerator {
    val DEFAULT = Provider(
        id = IdGenerator.getAndIncrement(),
        code = "N00",
        description = "NPS"
    )
}

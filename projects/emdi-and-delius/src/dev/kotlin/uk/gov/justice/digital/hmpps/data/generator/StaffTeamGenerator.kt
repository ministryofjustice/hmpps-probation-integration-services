package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.StaffTeam
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.entity.staff.User

object StaffTeamGenerator {
    // DEFAULT_USER assigned to multiple teams
    val DEFAULT_USER_TEAM_1 = generate(
        user = UserGenerator.DEFAULT_USER,
        team = TeamGenerator.DEFAULT
    )

    val DEFAULT_USER_TEAM_2 = generate(
        user = UserGenerator.DEFAULT_USER,
        team = TeamGenerator.SECOND
    )

    // LONDON_USER assigned to London team
    val LONDON_USER_TEAM = generate(
        user = UserGenerator.LONDON_USER,
        team = TeamGenerator.LONDON_TEAM
    )

    // MULTI_TEAM_USER assigned to multiple teams across regions
    val MULTI_TEAM_USER_TEAM_1 = generate(
        user = UserGenerator.MULTI_TEAM_USER,
        team = TeamGenerator.DEFAULT
    )

    val MULTI_TEAM_USER_TEAM_2 = generate(
        user = UserGenerator.MULTI_TEAM_USER,
        team = TeamGenerator.THIRD
    )

    val MULTI_TEAM_USER_TEAM_3 = generate(
        user = UserGenerator.MULTI_TEAM_USER,
        team = TeamGenerator.LONDON_TEAM
    )

    fun generate(
        user: User,
        team: Team
    ) = StaffTeam(
        staffId = requireNotNull(user.staffId) { "StaffTeam test data requires a user.staffId" },
        teamId = team.id,
        user = user,
        team = team
    )
}


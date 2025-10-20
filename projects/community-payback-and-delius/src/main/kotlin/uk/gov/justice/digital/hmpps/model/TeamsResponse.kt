package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.delius.Team

data class TeamsResponse(
    val teams: List<TeamDTO>
)

data class TeamDTO(
    val code: String,
    val description: String
)

fun Team.toTeamDTO() = TeamDTO(
    code = this.code,
    description = this.description
)
package uk.gov.justice.digital.hmpps.api.model.user

data class Team(
    val description: String,
    val code: String
)

data class TeamResponse(
    val teams: List<Team>
)

package uk.gov.justice.digital.hmpps.api.model.user

data class UserTeam(
    val provider: String?,
    val teams: List<Team>,
)

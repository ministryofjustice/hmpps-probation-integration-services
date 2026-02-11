package uk.gov.justice.digital.hmpps.model

data class SupervisorResponse(
    val code: String,
    val isUnpaidWorkTeamMember: Boolean,
    val unpaidWorkTeams: List<SupervisorTeamsResponse>,
)

data class SupervisorTeamsResponse(
    val code: String,
    val description: String,
    val provider: CodeDescription
)
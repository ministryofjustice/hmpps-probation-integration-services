package uk.gov.justice.digital.hmpps.api.model.user

data class TeamCaseload(
    val provider: String?,
    val team: Team,
    val caseload: List<TeamCase>
)

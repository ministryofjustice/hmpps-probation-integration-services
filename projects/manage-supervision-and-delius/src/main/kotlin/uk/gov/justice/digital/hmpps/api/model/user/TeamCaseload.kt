package uk.gov.justice.digital.hmpps.api.model.user

data class TeamCaseload(
    val totalPages: Int,
    val totalElements: Int,
    val provider: String?,
    val team: Team,
    val caseload: List<TeamCase>
)

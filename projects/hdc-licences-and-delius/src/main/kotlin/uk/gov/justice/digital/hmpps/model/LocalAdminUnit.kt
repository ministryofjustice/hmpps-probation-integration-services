package uk.gov.justice.digital.hmpps.model

data class LocalAdminUnit(
    val code: String,
    val description: String,
)

data class LocalAdminUnitWithTeams(
    val code: String,
    val description: String,
    val teams: List<Team>,
)

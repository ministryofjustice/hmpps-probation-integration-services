package uk.gov.justice.digital.hmpps.model

data class Staff(
    val code: String,
    val staffId: Long,
    val name: Name,
    val teams: List<TeamDetails>,
    val username: String?,
    val email: String?
)

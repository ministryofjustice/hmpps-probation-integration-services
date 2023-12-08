package uk.gov.justice.digital.hmpps.api.model

data class ActiveCasesResponse(
    val code: String,
    val name: Name,
    val grade: String?,
    val email: String?,
    val cases: List<Case>,
)

data class Case(
    val crn: String,
    val name: Name,
    val type: String,
)

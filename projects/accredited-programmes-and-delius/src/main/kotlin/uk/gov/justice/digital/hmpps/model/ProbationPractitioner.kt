package uk.gov.justice.digital.hmpps.model

data class ProbationPractitioner(
    val name: Name,
    val code: String,
    val email: String?
)

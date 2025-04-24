package uk.gov.justice.digital.hmpps.api.model

data class ProbationCase(
    val crn: String,
    val nomisId: String?,
    val pncNumber: String? = null,
    val croNumber: String? = null,
)
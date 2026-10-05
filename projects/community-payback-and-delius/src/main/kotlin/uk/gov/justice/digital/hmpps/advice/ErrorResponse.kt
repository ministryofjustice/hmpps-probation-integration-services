package uk.gov.justice.digital.hmpps.advice

data class ErrorResponse(
    val status: Int,
    val developerMessage: String? = null
)
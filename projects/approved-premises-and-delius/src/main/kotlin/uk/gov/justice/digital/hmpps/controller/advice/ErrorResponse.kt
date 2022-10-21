package uk.gov.justice.digital.hmpps.controller.advice

data class ErrorResponse(
    val status: Int,
    val message: String? = null,
)

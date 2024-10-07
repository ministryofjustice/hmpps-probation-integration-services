package uk.gov.justice.digital.hmpps.api.resource.advice

data class ErrorResponse(
    val status: Int,
    val developerMessage: String? = null
)

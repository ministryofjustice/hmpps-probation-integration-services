package uk.gov.justice.digital.hmpps.crimeportalgateway.model

data class MessageStatus(
    val status: String? = null,
    val code: String? = null,
    val reason: String? = null,
    val detail: String? = null,
)

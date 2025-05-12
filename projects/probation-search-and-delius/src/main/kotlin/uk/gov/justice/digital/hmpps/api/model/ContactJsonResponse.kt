package uk.gov.justice.digital.hmpps.api.model

data class ContactJsonResponse(
    val contactId: Long,
    val version: Long,
    val json: String
)
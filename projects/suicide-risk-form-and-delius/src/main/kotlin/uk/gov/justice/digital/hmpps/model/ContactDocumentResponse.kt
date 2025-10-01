package uk.gov.justice.digital.hmpps.model

import java.time.ZonedDateTime

data class ContactDocumentResponse(
    val content: List<ContactDocumentItem>
)

data class ContactDocumentItem(
    val id: Long,
    val documents: List<ContactDocumentDetails>
)

data class ContactDocumentDetails(
    val id: Long,
    val name: String,
    val lastUpdated: ZonedDateTime?,
    val alfrescoId: String
)

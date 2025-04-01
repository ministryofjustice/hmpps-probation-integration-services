package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import java.time.Instant
import java.time.ZonedDateTime

data class Document(
    val id: String,
    val name: String,
    val lastUpdated: ZonedDateTime?
)

data class PersonDocuments(
    val personSummary: PersonSummary,
    val totalPages: Int,
    val totalElements: Int,
    val sortedBy: String? = null,
    val documents: List<DocumentDetails>
)

data class DocumentDetails(
    val alfrescoId: String,
    val offenderId: Long,
    val name: String,
    val docLevel: String,
    val description: String,
    val createdAt: Instant?,
    val lastUpdatedAt: Instant?,
    val author: String?,
    val eventId: Long?,
    val sensitive: Boolean? = false,
)


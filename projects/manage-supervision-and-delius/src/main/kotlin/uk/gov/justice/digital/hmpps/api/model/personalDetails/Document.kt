package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.service.DocumentLevelCode
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class Document(
    val id: String,
    val name: String,
    val createdAt: ZonedDateTime?,
    val lastUpdated: ZonedDateTime?
)

data class PersonDocuments(
    val personSummary: PersonSummary,
    val totalPages: Int,
    val totalElements: Int,
    val sortedBy: String? = null,
    val documents: List<DocumentDetails>,
    val metadata: DocumentMetadata? = null
)

data class DocumentSearch(
    val name: String? = null,
    val dateFrom: LocalDateTime? = null,
    val dateTo: LocalDateTime? = null,
)

data class DocumentTextSearch(
    val query: String? = null,
    val levelCode: DocumentLevelCode = DocumentLevelCode.ALL,
    val dateFrom: LocalDateTime? = null,
    val dateTo: LocalDateTime? = null,
)

data class DocumentMetadata(
    val documentLevels: List<DocumentLevel> = emptyList(),
)

data class DocumentLevel(
    val code: String,
    val description: String,
)

data class DocumentDetails(
    val alfrescoId: String,
    val offenderId: Long,
    val name: String,
    val level: String,
    val type: String,
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null,
    val author: String?,
    val status: String? = null,
    val workInProgress: Boolean?
)


package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class OffenderDocumentDetail(
    val id: String? = null,
    val documentName: String? = null,
    val author: String? = null,
    val type: KeyValue,
    val extendedDescription: String? = null,
    val lastModifiedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val parentPrimaryKeyId: Long? = null,
    val subType: KeyValue? = null,
    val reportDocumentDates: ReportDocumentDates? = null
)

data class ReportDocumentDates(
    val requestedDate: LocalDate? = null,
    val requiredDate: LocalDate? = null,
    val completedDate: LocalDateTime? = null
)

data class ConvictionDocuments(
    val convictionId: String,
    val documents: List<OffenderDocumentDetail> = emptyList()
)

data class OffenderDocuments(
    val documents: List<OffenderDocumentDetail> = emptyList(),
    val convictions: List<ConvictionDocuments> = emptyList(),
)

data class DocumentFilter(
    val type: String? = null,
    val subtype: String? = null
)

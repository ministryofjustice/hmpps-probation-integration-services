package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.api.model.personalDetails.*
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getSummary
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentsRepository
import java.time.LocalTime

@Service
class DocumentsService(
    private val documentsRepository: DocumentsRepository,
    private val personRepository: PersonRepository,
    private val alfrescoClient: AlfrescoClient,
) {

    fun getDocuments(crn: String, pageable: Pageable, sortedBy: String): PersonDocuments {
        val summary = personRepository.getSummary(crn)
        val documents = documentsRepository.findByOffenderId(summary.id, pageable)
        return PersonDocuments(
            personSummary = summary.toPersonSummary(),
            totalElements = documents.totalElements.toInt(),
            totalPages = documents.totalPages,
            documents = documents.content.map { it.toDocumentDetails() },
            sortedBy = sortedBy
        )
    }

    fun search(documentSearch: DocumentSearch, crn: String, pageable: Pageable, sortedBy: String): PersonDocuments {
        val summary = personRepository.getSummary(crn)
        val documents = documentsRepository.searchWithFilename(
            summary.id,
            documentSearch.name,
            documentSearch.dateFrom?.toLocalDate()?.atStartOfDay(),
            documentSearch.dateTo?.toLocalDate()?.atTime(LocalTime.MAX),
            pageable
        )
        return PersonDocuments(
            personSummary = summary.toPersonSummary(),
            totalElements = documents.totalElements.toInt(),
            totalPages = documents.totalPages,
            documents = documents.content.map { it.toDocumentDetails() },
            sortedBy = sortedBy,
            metadata = metadata()
        )
    }

    fun textSearch(
        documentTextSearch: DocumentTextSearch,
        crn: String,
        pageable: Pageable,
        sortedBy: String?
    ): PersonDocuments {
        val summary = personRepository.getSummary(crn)
        val metadata = metadata()
        val ids = if (documentTextSearch.query.isNullOrBlank()) null else alfrescoClient.textSearch(
            crn,
            documentTextSearch.query
        ).documents.map { it.id }

        val documents = documentsRepository.search(
            summary.id,
            documentTextSearch.dateFrom?.toLocalDate()?.atStartOfDay(),
            documentTextSearch.dateTo?.toLocalDate()?.atTime(LocalTime.MAX),
            documentTextSearch.levelCode,
            ids,
            pageable
        )

        return PersonDocuments(
            personSummary = summary.toPersonSummary(),
            totalElements = documents.totalElements.toInt(),
            totalPages = documents.totalPages,
            documents = sortUsingSearchResults(ids, sortedBy, documents.content),
            sortedBy = sortedBy,
            metadata = metadata
        )
    }

    fun sortUsingSearchResults(
        ids: List<String>?,
        sortedBy: String?,
        documents: List<DocumentEntity>
    ): List<DocumentDetails> {
        if (sortedBy != null) {
            return documents.map { it.toDocumentDetails() }
        }
        return (ids?.mapNotNull { id -> documents.firstOrNull { it.alfrescoId == id } }
            ?: documents).map { it.toDocumentDetails() }
    }

    fun metadata(): DocumentMetadata {
        return DocumentMetadata(
            documentLevels = (listOf(DocumentLevelCode.ALL) + (DocumentLevelCode.entries.filter { it != DocumentLevelCode.ALL }
                .sortedBy { it.name })).map { DocumentLevel(it.name, it.description) }
        )
    }
}

enum class DocumentLevelCode(val description: String) {
    ALL("All documents"),
    PERSON("Person"),
    PRE_CONS("Pre Cons"),
    ADDRESS_ASSESSMENT("Address assessment"),
    PERSONAL_CONTACT("Personal contact"),
    PERSONAL_CIRCUMSTANCE("Personal circumstance"),
    EVENT("Event"),
    CPS("CPS Pack"),
    COURT_REPORT("Court report"),
    INSTITUTIONAL_REPORT("Institutional report"),
    AP_REFERRAL("AP Referral"),
    ASSESSMENT("Assessment"),
    CASE_ALLOCATION("Case allocation"),
    REFERRAL("Referral"),
    UPW("UPW Appointment"),
    CONTACT("Contact"),
    NSI("NSI"),
    ADDRESS("Address"),
    REGISTER("Register")
}

fun DocumentEntity.toDocumentDetails() = DocumentDetails(
    alfrescoId,
    offenderId,
    name,
    level,
    type,
    createdAt,
    lastUpdatedAt,
    author,
    status,
    workInProgress ?: false
)

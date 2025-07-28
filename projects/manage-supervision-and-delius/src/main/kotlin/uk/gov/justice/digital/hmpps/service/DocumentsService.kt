package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MultiValueMap
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.api.model.personalDetails.*
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.alfresco.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentsRepository
import java.time.LocalTime

@Service
class DocumentsService(
    auditedInteractionService: AuditedInteractionService,
    private val documentsRepository: DocumentsRepository,
    private val personRepository: PersonRepository,
    private val alfrescoClient: AlfrescoClient,
    private val alfrescoUploadClient: AlfrescoUploadClient,
    private val contactRepository: ContactRepository
) : AuditableService(auditedInteractionService) {

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
        useDBFilenameSearch: Boolean,
        pageable: Pageable,
        sortedBy: String?
    ): PersonDocuments {
        val summary = personRepository.getSummary(crn)
        val metadata = metadata()
        val ids = if (documentTextSearch.query.isNullOrBlank()) null else
            try {
                alfrescoClient.textSearch(crn, documentTextSearch.query).documents.map { it.id }
            } catch (_: Exception) {
                null
            }
        val keywords =
            if (!useDBFilenameSearch) null else documentTextSearch.query?.split("\\s+".toRegex())?.joinToString("|")
        val documents = documentsRepository.search(
            summary.id,
            documentTextSearch.dateFrom?.toLocalDate()?.atStartOfDay(),
            documentTextSearch.dateTo?.toLocalDate()?.atTime(LocalTime.MAX),
            documentTextSearch.levelCode,
            ids,
            keywords,
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

    @Transactional
    fun addDocument(crn: String, id: Long, file: MultipartFile): String {
        return audit(BusinessInteractionCode.UPLOAD_DOCUMENT) { audit ->
            val person = personRepository.getPerson(crn)
            audit["offenderId"] = person.id

            val contact = contactRepository.getContact(id)
            val alfrescoDocument =
                alfrescoUploadClient.addDocument(
                    populateBodyValues(
                        crn,
                        file.bytes,
                        file.originalFilename!!,
                        contact.id
                    )
                )

            return@audit alfrescoDocument.id
        }
    }

    private fun populateBodyValues(
        crn: String,
        file: ByteArray,
        filename: String,
        contactId: Long
    ): MultiValueMap<String, HttpEntity<*>> {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("CRN", crn, MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityId", contactId.toString(), MediaType.TEXT_PLAIN)
        bodyBuilder.part("author", "Service,Manage my Supervision", MediaType.TEXT_PLAIN)
        bodyBuilder.part("filedata", file, MediaType.APPLICATION_OCTET_STREAM)
            .filename(filename)
        bodyBuilder.part("docType", "DOCUMENT", MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityType", "CONTACT", MediaType.TEXT_PLAIN)

        return bodyBuilder.build()
    }

    fun sortUsingSearchResults(
        ids: List<String>?,
        sortedBy: String?,
        documents: List<DocumentEntity>
    ): List<DocumentDetails> {
        if (sortedBy != null) {
            return documents.map { it.toDocumentDetails() }
        }

        val filenameMatches = documents.filter { ids?.contains(it.alfrescoId) == false }.map { it.toDocumentDetails() }
        return (ids?.mapNotNull { id -> documents.firstOrNull { it.alfrescoId == id } }
            ?: documents).map { it.toDocumentDetails() } + filenameMatches
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

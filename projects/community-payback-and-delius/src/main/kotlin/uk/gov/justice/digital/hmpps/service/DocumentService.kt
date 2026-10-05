package uk.gov.justice.digital.hmpps.service

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.sentry.Sentry
import jakarta.persistence.EntityManager
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.client.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.client.RestClientUtils.nullIfNotFound
import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import java.time.ZonedDateTime
import java.util.*
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Service
@Transactional
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val alfrescoUploadClient: AlfrescoUploadClient,
    auditedInteractionService: AuditedInteractionService,
    private val entityManager: EntityManager,
) : AuditableService(auditedInteractionService) {

    fun uploadAppointmentDocument(
        appointment: UnpaidWorkAppointment,
        filename: String,
        file: ByteArray,
        userId: Long
    ): Document = audit(BusinessInteractionCode.UPLOAD_DOCUMENT) {
        validateFile(filename)

        val document = Document(
            person = appointment.person,
            alfrescoId = "",
            name = filename,
            primaryKeyId = appointment.contact.id,
            tableName = CONTACT_TABLE_NAME,
            externalReference = Document.communityPaybackUrn(UUID.randomUUID()),
            lastSaved = ZonedDateTime.now(),
            createdDatetime = ZonedDateTime.now(),
            createdByUserId = userId,
            lastUpdatedUserId = userId,
            workInProgress = "N",
            status = "Y",
            softDeleted = false,
            partitionAreaId = 0,
            id = 0,
        )

        val alfrescoId = alfrescoUploadClient.upload(document.toMultipart(file, appointment.person)).id
        document.alfrescoId = alfrescoId

        populateAudit(document, it)

        val savedDocument = documentRepository.save(document)
        updateContactDocumentLinked(appointment.contact.id, true)

        savedDocument
    }

    fun deleteDocument(document: Document) = audit(BusinessInteractionCode.DELETE_DOCUMENT) {
        populateAudit(document, it)

        try {
            alfrescoUploadClient.release(document.alfrescoId)
        } catch (e: Exception) {
            Span.current().recordException(e).setStatus(StatusCode.ERROR)
            Sentry.captureException(e)
        }
        nullIfNotFound { alfrescoUploadClient.delete(document.alfrescoId) }

        documentRepository.delete(document)
        val hasDocuments = documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNotAndSoftDeletedFalse(
            document.tableName,
            document.primaryKeyId,
            document.id
        )
        updateContactDocumentLinked(document.primaryKeyId, hasDocuments)
    }

    fun deleteDocumentByAppointmentAndDocumentId(
        appointmentId: Long,
        documentId: Long,
        appointment: UnpaidWorkAppointment
    ) {
        val document = documentRepository.findById(documentId)
            .orElseThrow {
                NotFoundException("Document", "id", documentId)
            }

        require(document.tableName == CONTACT_TABLE_NAME && document.primaryKeyId == appointment.contact.id) {
            "Document $documentId does not belong to appointment $appointmentId"
        }

        deleteDocument(document)
    }

    private fun updateContactDocumentLinked(contactId: Long, hasDocuments: Boolean) {
        entityManager.createNativeQuery(
            "update contact set document_linked = :documentLinked where contact_id = :contactId"
        )
            .setParameter("documentLinked", if (hasDocuments) "Y" else "N")
            .setParameter("contactId", contactId)
            .executeUpdate()
    }

    fun validateFile(filename: String) {
        val extension = filename.substringAfterLast(".").lowercase()
        require(ALLOWED_EXTENSIONS.contains(extension)) {
            "File extension '$extension' is not allowed. Allowed extensions: ${ALLOWED_EXTENSIONS.joinToString(", ")}"
        }
    }

    private fun Document.toMultipart(file: ByteArray, person: Person) =
        MultipartBodyBuilder().apply {
            part("CRN", person.crn, MediaType.TEXT_PLAIN)
            part("fileName", name, MediaType.TEXT_PLAIN)
            part("filedata", file, MediaType.APPLICATION_OCTET_STREAM).filename(name)
            part("author", "Service,Community Payback", MediaType.TEXT_PLAIN)
            part("docType", "DOCUMENT", MediaType.TEXT_PLAIN)
            part("entityType", "CONTACT", MediaType.TEXT_PLAIN)
            part("entityId", primaryKeyId.toString(), MediaType.TEXT_PLAIN)
            part("locked", "true", MediaType.TEXT_PLAIN)
        }.build()

    private fun populateAudit(document: Document, audit: AuditedInteraction.Parameters) {
        audit["documentId"] = document.id
        audit["alfrescoDocumentId"] = document.alfrescoId
        audit["entityId"] = document.primaryKeyId
        audit["tableName"] = document.tableName
        audit["externalReference"] = document.externalReference
    }

    companion object {
        private const val CONTACT_TABLE_NAME = "CONTACT"

        val ALLOWED_EXTENSIONS = setOf(
            "doc", "docx", "rtf", "txt", "dot", "dotm", "docm", "odt", "xml", "wpd", "wri", "wps",
            "xls", "xlsb", "xlsx", "csv", "pdf", "bmp", "jpg", "jpeg", "gif", "png",
            "m4a", "flac", "mp3", "mp4", "wav", "wma", "aac"
        )
    }
}

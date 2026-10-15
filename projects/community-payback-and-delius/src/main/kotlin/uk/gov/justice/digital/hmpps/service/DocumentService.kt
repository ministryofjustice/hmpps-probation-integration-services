package uk.gov.justice.digital.hmpps.service

import jakarta.persistence.EntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.client.AlfrescoClient
import uk.gov.justice.digital.hmpps.client.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.client.RestClientUtils.nullIfNotFound
import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.getAppointment
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.DocumentListResponse
import uk.gov.justice.digital.hmpps.model.DocumentUploadResponse
import java.util.*

@Service
@Transactional
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val alfrescoClient: AlfrescoClient,
    private val alfrescoUploadClient: AlfrescoUploadClient,
    private val entityManager: EntityManager,
    auditedInteractionService: AuditedInteractionService,
) : AuditableService(auditedInteractionService) {

    fun getDocuments(appointmentId: Long): DocumentListResponse {
        val contactId = unpaidWorkAppointmentRepository.getContactIdForUnpaidWorkAppointment(appointmentId)
            ?: throw NotFoundException("UnpaidWorkAppointment", "id", appointmentId)
        return DocumentListResponse(
            content = documentRepository.findAllByPrimaryKeyIdAndTableName(contactId, "CONTACT").map {
                DocumentUploadResponse(it.id, it.name)
            }
        )
    }

    fun downloadDocument(appointmentId: Long, documentId: Long): ResponseEntity<StreamingResponseBody> {
        val contactId = unpaidWorkAppointmentRepository.getContactIdForUnpaidWorkAppointment(appointmentId)
            ?: throw NotFoundException("UnpaidWorkAppointment", "id", appointmentId)
        val document = documentRepository.findByIdOrNull(documentId).orNotFoundBy("id", documentId)
        require(document.primaryKeyId == contactId) { "Document $documentId does not belong to appointment $contactId" }
        return alfrescoClient.streamDocument(document.alfrescoId, document.name)
    }

    fun uploadAppointmentDocument(
        appointmentId: Long,
        file: MultipartFile,
    ): DocumentUploadResponse = audit(BusinessInteractionCode.UPLOAD_DOCUMENT) {
        val filename = file.originalFilename ?: "document"
        validateFile(filename)
        val appointment = unpaidWorkAppointmentRepository.getAppointment(appointmentId)

        val document = Document(
            person = appointment.person,
            alfrescoId = "",
            name = filename,
            primaryKeyId = appointment.contact.id,
            tableName = "CONTACT",
            externalReference = Document.communityPaybackUrn(UUID.randomUUID()),
            workInProgress = "N",
            status = "Y",
        )

        val alfrescoId = alfrescoUploadClient.upload(document.toMultipart(file.bytes, appointment.person)).id
        document.alfrescoId = alfrescoId

        populateAudit(document, it)

        val savedDocument = documentRepository.save(document)
        updateContactDocumentLinked(appointment.contact.id, true)

        DocumentUploadResponse(savedDocument.id, savedDocument.name)
    }

    fun deleteDocument(document: Document) = audit(BusinessInteractionCode.DELETE_DOCUMENT) {
        populateAudit(document, it)

        alfrescoUploadClient.release(document.alfrescoId)
        nullIfNotFound { alfrescoUploadClient.delete(document.alfrescoId) }

        documentRepository.delete(document)
        val hasDocuments = documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNot(
            document.tableName,
            document.primaryKeyId,
            document.id
        )
        updateContactDocumentLinked(document.primaryKeyId, hasDocuments)
    }

    fun deleteDocumentById(appointmentId: Long, documentId: Long) {
        val document = documentRepository.findByIdOrNull(documentId).orNotFoundBy("id", documentId)

        require(document.primaryKeyId == appointmentId) {
            "Document $documentId does not belong to appointment $appointmentId"
        }

        deleteDocument(document)
    }

    private fun updateContactDocumentLinked(contactId: Long, hasDocuments: Boolean) {
        entityManager.createNativeQuery("update contact set document_linked = :documentLinked where contact_id = :contactId")
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
        val ALLOWED_EXTENSIONS = setOf(
            "doc", "docx", "rtf", "txt", "dot", "dotm", "docm", "odt", "xml", "wpd", "wri", "wps",
            "xls", "xlsb", "xlsx", "csv", "pdf", "bmp", "jpg", "jpeg", "gif", "png",
            "m4a", "flac", "mp3", "mp4", "wav", "wma", "aac"
        )
    }
}

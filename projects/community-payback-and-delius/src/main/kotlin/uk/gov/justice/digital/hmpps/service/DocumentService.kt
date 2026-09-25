package uk.gov.justice.digital.hmpps.service

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

@Service
@Transactional
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val alfrescoUploadClient: AlfrescoUploadClient,
    private val entityManager: EntityManager,
) {

    fun uploadAppointmentDocument(
        appointment: UnpaidWorkAppointment,
        filename: String,
        file: ByteArray,
        userId: Long
    ): Document {
        validateFile(filename, file)

        val document = Document(
            person = appointment.person,
            alfrescoId = "",
            name = filename,
            primaryKeyId = appointment.contact.id,
            tableName = "CONTACT",
            externalReference = Document.communityPaybackUrn(UUID.randomUUID()),
            lastSaved = ZonedDateTime.now(),
            createdDatetime = ZonedDateTime.now(),
            createdByUserId = userId,
            lastUpdatedUserId = userId,
            workInProgress = "N",
            status = "Y",
            softDeleted = false,
            id = 0,
        )

        val alfrescoId = alfrescoUploadClient.upload(document.toMultipart(file, appointment.person)).id
        document.alfrescoId = alfrescoId

        val savedDocument = documentRepository.save(document)
        updateContactDocumentLinked(appointment.contact.id, true)

        return savedDocument
    }

    fun deleteDocument(document: Document) {
        nullIfNotFound { alfrescoUploadClient.delete(document.alfrescoId) }

        documentRepository.delete(document)
        val hasDocuments = documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNotAndSoftDeletedFalse(
            document.tableName,
            document.primaryKeyId,
            document.id
        )
        updateContactDocumentLinked(document.primaryKeyId, hasDocuments)
    }

    private fun updateContactDocumentLinked(contactId: Long, hasDocuments: Boolean) {
        entityManager.createNativeQuery(
            "update contact set document_linked = :documentLinked where contact_id = :contactId"
        )
            .setParameter("documentLinked", if (hasDocuments) "Y" else "N")
            .setParameter("contactId", contactId)
            .executeUpdate()
    }

    fun validateFile(filename: String, file: ByteArray) {
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

    companion object {
        val ALLOWED_EXTENSIONS = setOf(
            "doc", "docx", "rtf", "txt", "dot", "dotm", "docm", "odt", "xml", "wpd", "wri", "wps",
            "xls", "xlsb", "xlsx", "csv", "pdf", "bmp", "jpg", "jpeg", "gif", "png",
            "m4a", "flac", "mp3", "mp4", "wav", "wma", "aac"
        )
    }
}

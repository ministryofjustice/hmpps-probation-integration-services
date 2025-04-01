package uk.gov.justice.digital.hmpps.service

import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.client.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.Document
import uk.gov.justice.digital.hmpps.integrations.delius.Document.Companion.breachNoticeUrn
import uk.gov.justice.digital.hmpps.integrations.delius.DocumentRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.security.ServiceContext
import java.time.ZonedDateTime
import java.util.*

@Service
class DocumentService(
    auditedInteractionService: AuditedInteractionService,
    private val documentRepository: DocumentRepository,
    private val alfrescoUploadClient: AlfrescoUploadClient,
) : AuditableService(auditedInteractionService) {
    fun uploadDocument(event: HmppsDomainEvent, file: ByteArray) = audit(BusinessInteractionCode.UPLOAD_DOCUMENT) {
        check(file.isPdf()) { "Invalid PDF file: ${event.detailUrl}" }

        val document = getDocument(event, it)
        document.name = document.name.replace(Regex("\\.docx?$"), ".pdf")
        document.status = "Y"
        document.workInProgress = "N"
        document.lastSaved = ZonedDateTime.now()
        document.lastUpdatedUserId = ServiceContext.servicePrincipal()!!.userId
        documentRepository.save(document)

        alfrescoUploadClient.release(document.alfrescoId)
        alfrescoUploadClient.update(document.alfrescoId, document.toMultipart(file))
        alfrescoUploadClient.lock(document.alfrescoId)
    }

    fun deleteDocument(event: HmppsDomainEvent) = audit(BusinessInteractionCode.DELETE_DOCUMENT) {
        val document = getDocument(event, it)
        documentRepository.delete(document)
        alfrescoUploadClient.release(document.alfrescoId)
        alfrescoUploadClient.delete(document.alfrescoId)
    }

    private fun getDocument(event: HmppsDomainEvent, audit: AuditedInteraction.Parameters): Document {
        val urn = breachNoticeUrn(UUID.fromString(event.additionalInformation["breachNoticeId"] as String))
        return documentRepository.findByExternalReference(urn)?.also {
            audit["documentId"] = it.id
            audit["alfrescoDocumentId"] = it.alfrescoId
            audit["entityId"] = it.primaryKeyId
            audit["tableName"] = it.tableName
            audit["externalReference"] = urn
        } ?: throw NotFoundException("Document", "externalReference", urn)
    }

    private fun Document.toMultipart(file: ByteArray) = MultipartBodyBuilder().apply {
        part("CRN", person.crn, MediaType.TEXT_PLAIN)
        part("filedata", file, MediaType.APPLICATION_OCTET_STREAM).filename(name)
        part("author", "Service,Breach Notice", MediaType.TEXT_PLAIN)
        part("docType", "DOCUMENT", MediaType.TEXT_PLAIN)
        part("entityType", tableName, MediaType.TEXT_PLAIN)
        part("entityId", primaryKeyId.toString(), MediaType.TEXT_PLAIN)
    }.build()

    private fun ByteArray.isPdf() = take(4).toByteArray().contentEquals("%PDF".toByteArray())
}

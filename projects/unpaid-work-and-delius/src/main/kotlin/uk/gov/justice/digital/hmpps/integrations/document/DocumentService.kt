package uk.gov.justice.digital.hmpps.integrations.document

import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MultiValueMap
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.document.Document
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent

@Service
class DocumentService(
    auditedInteractionService: AuditedInteractionService,
    private val documentRepository: DocumentRepository,
    private val alfrescoClient: AlfrescoClient
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun createDeliusDocument(hmppsEvent: HmppsDomainEvent, file: ByteArray, filename: String, contactId: Long, episodeId: String, offenderId: Long) =
        audit(BusinessInteractionCode.UPLOAD_DOCUMENT) {
            val alfrescoDocument = alfrescoClient.addDocument(populateBodyValues(hmppsEvent, file, filename, contactId))
            documentRepository.save(
                Document(
                    contactId = contactId,
                    offenderId = offenderId,
                    alfrescoId = alfrescoDocument.id,
                    name = filename,
                    externalReference = episodeId
                )
            )
        }

    private fun populateBodyValues(
        hmppsEvent: HmppsDomainEvent,
        file: ByteArray,
        filename: String,
        contactId: Long,
    ): MultiValueMap<String, HttpEntity<*>> {
        val crn = hmppsEvent.personReference.findCrn()!!
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("CRN", crn, MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityId", contactId, MediaType.TEXT_PLAIN)
        bodyBuilder.part("author", "Service,UPW", MediaType.TEXT_PLAIN)
        bodyBuilder.part("filedata", file, MediaType.APPLICATION_OCTET_STREAM)
            .filename(filename)
        bodyBuilder.part("docType", "DOCUMENT", MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityType", "CONTACT", MediaType.TEXT_PLAIN)

        return bodyBuilder.build()
    }
}

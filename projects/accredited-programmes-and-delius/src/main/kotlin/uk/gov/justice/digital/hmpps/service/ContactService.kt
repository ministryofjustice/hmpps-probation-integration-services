package uk.gov.justice.digital.hmpps.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.aspect.UserContext
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integration.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.repository.ContactRepository

@Service
class ContactService (
    private val contactRepository: ContactRepository,
    private val auditedInteractionService: AuditedInteractionService,
    private val alfrescoUploadClient: AlfrescoUploadClient,
) : AuditableService(auditedInteractionService){

    fun hardDeleteContact(contactId: Long) {
        val contact = contactRepository.findByIdOrNull(contactId).orNotFoundBy("Contact ID", contactId)
        return audit(BusinessInteractionCode.DELETE_CONTACT, username = UserContext.get()?.username) { audit ->
            audit["contactId"] = contactId
            audit["offenderId"] = contact.person.id
            contactRepository.deleteById(contactId)
            contact.contactDocuments?.forEach { contactDocument ->
                audit["documentId"] = contactDocument.id
                alfrescoUploadClient.deleteDocument(contactDocument.id.toString())
            }
        }
    }
}
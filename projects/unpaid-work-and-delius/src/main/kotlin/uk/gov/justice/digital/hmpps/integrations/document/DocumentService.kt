package uk.gov.justice.digital.hmpps.integrations.document

import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MultiValueMap
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManager
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.time.ZonedDateTime

@Service
class DocumentService(
    auditedInteractionService: AuditedInteractionService,
    private val documentRepository: DocumentRepository,
    private val alfrescoUploadClient: AlfrescoUploadClient,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository

) : AuditableService(auditedInteractionService) {
    @Transactional
    fun createDeliusDocument(
        hmppsEvent: HmppsDomainEvent,
        file: ByteArray,
        filename: String,
        episodeId: String,
        person: PersonWithManager,
        eventId: Long,
        contactDate: ZonedDateTime
    ) =
        audit(BusinessInteractionCode.UPLOAD_DOCUMENT) {
            val externalReference = "urn:hmpps:unpaid-work-assessment:$episodeId"
            val contactId = createContact(person, eventId, contactDate, externalReference)

            val alfrescoDocument =
                alfrescoUploadClient.addDocument(populateBodyValues(hmppsEvent, file, filename, contactId))
            documentRepository.save(
                Document(
                    contactId = contactId,
                    offenderId = person.id,
                    alfrescoId = alfrescoDocument.id,
                    name = filename,
                    externalReference = externalReference,
                    tableName = "CONTACT"
                )
            )
        }

    private fun createContact(
        person: PersonWithManager,
        eventId: Long,
        date: ZonedDateTime,
        externalReference: String
    ): Long {
        val manager = person.managers.first()
        val staffId = manager.staff.id
        val teamId = manager.team.id

        val contact = contactRepository.save(
            Contact(
                notes = "CP/UPW Assessment",
                date = date,
                person = person,
                eventId = eventId,
                startTime = date,
                staffId = staffId,
                teamId = teamId,
                type = contactTypeRepository.getByCode(ContactTypeCode.UPW_ASSESSMENT.code),
                documentLinked = true,
                externalReference = externalReference
            )
        )
        return contact.id
    }

    private fun populateBodyValues(
        hmppsEvent: HmppsDomainEvent,
        file: ByteArray,
        filename: String,
        contactId: Long
    ): MultiValueMap<String, HttpEntity<*>> {
        val crn = hmppsEvent.personReference.findCrn()!!
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("CRN", crn, MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityId", contactId.toString(), MediaType.TEXT_PLAIN)
        bodyBuilder.part("author", "Service,UPW", MediaType.TEXT_PLAIN)
        bodyBuilder.part("filedata", file, MediaType.APPLICATION_OCTET_STREAM)
            .filename(filename)
        bodyBuilder.part("docType", "DOCUMENT", MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityType", "CONTACT", MediaType.TEXT_PLAIN)

        return bodyBuilder.build()
    }
}

package uk.gov.justice.digital.hmpps.integrations.upwassessment

import org.springframework.http.ContentDisposition
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.arn.ArnClient
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManager
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.document.DocumentService
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Service
class UPWAssessmentService(
    private val telemetryService: TelemetryService,
    private val documentService: DocumentService,
    private val personWithManagerRepository: PersonWithManagerRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val arnClient: ArnClient
) {
    fun AdditionalInformation.episodeId() = this["episodeId"] as String

    @Transactional
    fun processMessage(notification: Notification<HmppsDomainEvent>) {
        val crn = notification.message.personReference.findCrn()!!
        val person = personWithManagerRepository.findByCrnAndSoftDeletedIsFalse(crn) ?: return let {
            telemetryService.trackEvent(
                "PersonNotFound",
                mapOf("crn" to notification.message.personReference.findCrn()!!)
            )
        }
        val contactId = createContact(person, notification)
        uploadDocument(notification, contactId, person.id)
    }

    private fun createContact(
        person: PersonWithManager,
        notification: Notification<HmppsDomainEvent>
    ): Long {
        val manager = person.managers.first()
        val staffId = manager.staff.id
        val teamId = manager.team.id

        val contact = contactRepository.save(
            Contact(
                notes = "CP/UPW Assessment",
                date = notification.message.occurredAt,
                person = person,
                startTime = notification.message.occurredAt,
                staffId = staffId,
                teamId = teamId,
                type = contactTypeRepository.getByCode(ContactTypeCode.UPW_ASSESSMENT.code)
            )
        )
        return contact.id
    }

    private fun uploadDocument(notification: Notification<HmppsDomainEvent>, contactId: Long, offenderId: Long) {
        // get the episode id from the message then get the document content from the UPW/ARN Service
        val episodeId = notification.message.additionalInformation.episodeId()
        val response = arnClient.getUPWAssessment(URI(notification.message.detailUrl!!))
        val filename = ContentDisposition.parse(response.headers()["Content-Disposition"]!!.first()).filename
        val fileData = response.body().asInputStream().readAllBytes()
        if (response.status() != 200 || !fileData.isPdf()) {
            throw IllegalStateException("Invalid PDF returned for episode: ${notification.message.detailUrl}")
        }
        // Then upload the document content to Alfresco AND create a delius document that links to the alfresco id
        // and contact id.
        documentService.createDeliusDocument(notification.message, fileData, filename!!, contactId, episodeId, offenderId)
    }

    private fun ByteArray.isPdf() = take(4).toByteArray().contentEquals("%PDF".toByteArray())
}

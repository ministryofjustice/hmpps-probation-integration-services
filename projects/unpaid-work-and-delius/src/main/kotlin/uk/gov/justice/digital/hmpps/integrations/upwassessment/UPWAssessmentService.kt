package uk.gov.justice.digital.hmpps.integrations.upwassessment

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.EventRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.arn.ArnClient
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManager
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManagerRepository
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
    private val eventRepository: EventRepository,
    private val arnClient: ArnClient
) {
    fun AdditionalInformation.episodeId() = this["episodeId"] as String

    fun processMessage(notification: Notification<HmppsDomainEvent>) {
        val crn = notification.message.personReference.findCrn()!!
        val person = personWithManagerRepository.findByCrnAndSoftDeletedIsFalse(crn)
            ?: return let { telemetryService.trackEvent("PersonNotFound", mapOf("crn" to crn)) }
        val eventId = (notification.message.additionalInformation["eventId"] as String).toLong()
        if (!eventRepository.existsById(eventId)) throw NotFoundException("Event", "id", eventId)

        uploadDocument(notification, person, eventId)
    }

    private fun uploadDocument(notification: Notification<HmppsDomainEvent>, person: PersonWithManager, eventId: Long) {
        // get the episode id from the message then get the document content from the UPW/ARN Service
        val episodeId = notification.message.additionalInformation.episodeId()
        val response = arnClient.getUPWAssessment(URI(notification.message.detailUrl!!))
        val reg = Regex("[^A-Za-z0-9-. ]")
        val filename = "${person.forename}-${person.surname}-${person.crn}-UPW.pdf".replace(reg, "")
        val fileData = response.body
        check(response.statusCode == HttpStatus.OK && fileData?.isPdf() == true) { "Invalid PDF returned for episode: ${notification.message.detailUrl}" }
        // Then upload the document content to Alfresco AND create a delius document that links to the alfresco id
        // and contact id.
        try {
            documentService.createDeliusDocument(
                notification.message,
                fileData!!,
                filename,
                episodeId,
                person,
                eventId,
                notification.message.occurredAt
            )
        } catch (e: DataIntegrityViolationException) {
            if (e.isUniqueConstraintViolation()) {
                return telemetryService.trackEvent(
                    "DuplicateMessageReceived",
                    mapOf(
                        "episodeId" to notification.message.additionalInformation.episodeId(),
                        "crn" to notification.message.personReference.findCrn()!!
                    )
                )
            }
            throw e
        }
    }

    private fun ByteArray.isPdf() = take(4).toByteArray().contentEquals("%PDF".toByteArray())

    private fun DataIntegrityViolationException.isUniqueConstraintViolation(): Boolean =
        message?.let {
            it.contains("XAK2CONTACT") || it.contains("XIE10DOCUMENT")
        } ?: false
}

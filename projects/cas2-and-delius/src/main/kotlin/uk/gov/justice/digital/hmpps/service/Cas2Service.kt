package uk.gov.justice.digital.hmpps.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.ApplicationStatusUpdated
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.ApplicationSubmitted
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.EventDetails
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.entity.ContactType
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.HmppsDomainEventExtensions.crn
import uk.gov.justice.digital.hmpps.messaging.HmppsDomainEventExtensions.telemetryProperties
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
@Transactional
class Cas2Service(
    private val detailService: DomainEventDetailService,
    private val contactService: ContactService,
    private val telemetryService: TelemetryService,
) {

    fun applicationSubmitted(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<ApplicationSubmitted>>(event)
        val success = contactService.createContact(
            crn = event.crn,
            type = ContactType.REFERRAL_SUBMITTED,
            date = details.eventDetails.submittedAt,
            notes = "Details of the application can be found here: ${details.eventDetails.applicationUrl}",
            urn = "urn:hmpps:cas2:application-submitted:${details.eventDetails.applicationId}",
            description = "CAS2 Referral Submitted: ${details.eventDetails.applicationOrigin()}"
        )
        if (success) telemetryService.trackEvent(
            "ApplicationSubmitted",
            event.telemetryProperties + mapOf(
                "applicationId" to details.eventDetails.applicationId,
                "applicationOrigin" to details.eventDetails.applicationOrigin()
            ),
        )
    }

    fun applicationStatusUpdated(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<ApplicationStatusUpdated>>(event)
        val statusDetailsList = details.eventDetails.newStatus.statusDetails
            ?.joinToString("${System.lineSeparator()}|* ", "${System.lineSeparator()}|* ") { it.label } ?: ""
        val success = contactService.createContact(
            crn = event.crn,
            type = ContactType.REFERRAL_UPDATED,
            date = details.eventDetails.updatedAt,
            description = "CAS2 ${details.eventDetails.applicationOrigin()}".trim() + " Referral Updated - ${details.eventDetails.newStatus.label}",
            notes = """
                |Application status was updated to: ${details.eventDetails.newStatus.label}
                |
                |Details: ${details.eventDetails.newStatus.description}$statusDetailsList
                |
                |Details of the application can be found here: ${details.eventDetails.applicationUrl}
                """.trimMargin(),
            urn = "urn:hmpps:cas2:application-status-updated:${details.id}",
        )
        if (success) telemetryService.trackEvent(
            "ApplicationStatusUpdated", event.telemetryProperties + mapOf(
                "applicationId" to details.eventDetails.applicationId,
                "status" to details.eventDetails.newStatus.name
            )
        )
    }
}
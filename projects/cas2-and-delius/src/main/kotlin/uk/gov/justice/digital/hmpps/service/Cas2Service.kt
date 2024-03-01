package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.client.approvedpremises.EventDetailsClient
import uk.gov.justice.digital.hmpps.entity.ContactType
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.HmppsDomainEventExtensions.crn
import uk.gov.justice.digital.hmpps.messaging.HmppsDomainEventExtensions.telemetryProperties
import uk.gov.justice.digital.hmpps.messaging.HmppsDomainEventExtensions.url
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
@Transactional
class Cas2Service(
    private val eventDetailsClient: EventDetailsClient,
    private val contactService: ContactService,
    private val telemetryService: TelemetryService,
) {

    fun applicationSubmitted(event: HmppsDomainEvent) {
        val details = eventDetailsClient.getApplicationSubmittedDetails(event.url)
        val success = contactService.createContact(
            crn = event.crn,
            type = ContactType.REFERRAL_SUBMITTED,
            date = details.eventDetails.submittedAt,
            notes = "Details of the application can be found here: ${details.eventDetails.applicationUrl}",
            urn = "urn:hmpps:cas2:application-submitted:${details.eventDetails.applicationId}",
        )
        if (success) telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties)
    }

    fun applicationStatusUpdated(event: HmppsDomainEvent) {
        val details = eventDetailsClient.getApplicationStatusUpdatedDetails(event.url)
        val statusDetailList = details.eventDetails.newStatus.statusDetails
            .map { statusDetails -> statusDetails.name }
            .toList()
        val statusDetailsBuilder = StringBuilder()
        statusDetailList.forEach { name -> statusDetailsBuilder.append("* $name\n") }
        val notesBuilder = StringBuilder()
        notesBuilder.append("Application status was updated to: ${details.eventDetails.newStatus.label} - ${details.eventDetails.newStatus.description}\n\n")
        notesBuilder.append("Details: More information about the application has been requested from the POM (Prison Offender Manager).\n")
        notesBuilder.append(statusDetailsBuilder).append("\n")
        notesBuilder.append("Details of the application can be found here: ${details.eventDetails.applicationUrl}")

        val success = contactService.createContact(
            crn = event.crn,
            type = ContactType.REFERRAL_UPDATED,
            date = details.eventDetails.updatedAt,
            description = "CAS2 Referral Updated - ${details.eventDetails.newStatus.label}",
            notes = notesBuilder.toString(),
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
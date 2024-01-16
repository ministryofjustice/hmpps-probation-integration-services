package uk.gov.justice.digital.hmpps.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.client.approvedpremises.EventDetailsClient
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.HmppsDomainEventExtensions.crn
import uk.gov.justice.digital.hmpps.messaging.HmppsDomainEventExtensions.telemetryProperties
import uk.gov.justice.digital.hmpps.messaging.HmppsDomainEventExtensions.url
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
@Transactional
class Cas2Service(
    private val eventDetailsClient: EventDetailsClient,
    private val personRepository: PersonRepository,
    private val contactService: ContactService,
    private val personManagerRepository: PersonManagerRepository,
    private val telemetryService: TelemetryService,
) {

    fun applicationSubmitted(event: HmppsDomainEvent) {
        val details = eventDetailsClient.getApplicationSubmittedDetails(event.url).eventDetails
        val person = personRepository.getByCrn(event.crn)
        val manager = personManagerRepository.getActiveManager(person.id)
        val success = contactService.createContact(
            personId = person.id,
            type = ContactType.REFERRAL_SUBMITTED,
            date = details.submittedAt,
            manager = manager,
            notes = "Details of the application can be found here: ${details.applicationUrl}",
            urn = "urn:hmpps:cas2:application-submitted:${details.applicationId}",
        )
        if (success) telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties)
    }
}
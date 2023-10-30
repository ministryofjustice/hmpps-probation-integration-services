package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.Cas3ApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.AddressService
import uk.gov.justice.digital.hmpps.integrations.delius.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrn
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val contactService: ContactService,
    private val addressService: AddressService,
    private val cas3ApiClient: Cas3ApiClient,
    private val personRepository: PersonRepository
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val event = notification.message
        when (event.eventType) {
            "accommodation.cas3.referral.submitted" -> {
                contactService.createContact(event.crn()) {
                    cas3ApiClient.getApplicationSubmittedDetails(event.url())
                }
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }

            "accommodation.cas3.booking.cancelled" -> {
                contactService.createContact(event.crn()) {
                    cas3ApiClient.getBookingCancelledDetails(event.url())
                }
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }

            "accommodation.cas3.booking.confirmed" -> {
                contactService.createContact(event.crn()) {
                    cas3ApiClient.getBookingConfirmedDetails(event.url())
                }
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }

            "accommodation.cas3.booking.provisionally-made" -> {
                contactService.createContact(event.crn()) {
                    cas3ApiClient.getBookingProvisionallyMade(event.url())
                }
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }

            "accommodation.cas3.person.arrived" -> {
                val person = personRepository.getByCrn(event.crn())
                val detail = cas3ApiClient.getPersonArrived(event.url())
                contactService.createContact(event.crn(), person) {
                    detail
                }
                addressService.updateMainAddress(person, detail.eventDetails)
                telemetryService.trackEvent("PersonArrived", event.telemetryProperties())
            }

            "accommodation.cas3.person.departed" -> {
                contactService.createContact(event.crn()) {
                    cas3ApiClient.getPersonDeparted(event.url())
                }
                telemetryService.trackEvent("PersonDeparted", event.telemetryProperties())
            }

            else -> throw IllegalArgumentException("Unexpected event type ${event.eventType}")
        }
    }

    fun HmppsDomainEvent.telemetryProperties() = mapOf(
        "occurredAt" to occurredAt.toString(),
        "crn" to crn()
    )
}

fun HmppsDomainEvent.crn(): String = personReference.findCrn() ?: throw IllegalArgumentException("Missing CRN")

fun HmppsDomainEvent.url(): URI = URI.create(detailUrl ?: throw IllegalArgumentException("Missing detail url"))

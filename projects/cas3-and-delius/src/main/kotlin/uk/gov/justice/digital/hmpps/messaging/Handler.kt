package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.Cas3ApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.AddressService
import uk.gov.justice.digital.hmpps.integrations.delius.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrn
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Component
@Transactional
@Channel("cas3-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val contactService: ContactService,
    private val addressService: AddressService,
    private val cas3ApiClient: Cas3ApiClient,
    private val personRepository: PersonRepository
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(
                messageId = "accommodation.cas3.referral.submitted",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "accommodation.cas3.booking.cancelled",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "accommodation.cas3.booking.confirmed",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "accommodation.cas3.booking.provisionally-made",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "accommodation.cas3.person.arrived",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "accommodation.cas3.person.departed",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "accommodation.cas3.person.arrived.updated",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "accommodation.cas3.person.departed.updated",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "accommodation.cas3.booking.cancelled.updated",
                payload = Schema(HmppsDomainEvent::class)
            ),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val event = notification.message
        when (event.eventType) {
            "accommodation.cas3.referral.submitted" -> {
                contactService.createOrUpdateContact(event.crn()) {
                    cas3ApiClient.getApplicationSubmittedDetails(event.url())
                }
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }

            "accommodation.cas3.booking.cancelled" -> {
                contactService.createOrUpdateContact(event.crn()) {
                    cas3ApiClient.getBookingCancelledDetails(event.url())
                }
                telemetryService.trackEvent("BookingCancelled", event.telemetryProperties())
            }

            "accommodation.cas3.booking.confirmed" -> {
                contactService.createOrUpdateContact(event.crn()) {
                    cas3ApiClient.getBookingConfirmedDetails(event.url())
                }
                telemetryService.trackEvent("BookingConfirmed", event.telemetryProperties())
            }

            "accommodation.cas3.booking.provisionally-made" -> {
                contactService.createOrUpdateContact(event.crn()) {
                    cas3ApiClient.getBookingProvisionallyMade(event.url())
                }
                telemetryService.trackEvent("BookingProvisionallyMade", event.telemetryProperties())
            }

            "accommodation.cas3.person.arrived" -> {
                val person = personRepository.getByCrn(event.crn())
                val detail = cas3ApiClient.getPersonArrived(event.url())
                contactService.createOrUpdateContact(event.crn(), person) {
                    detail
                }
                addressService.updateMainAddress(person, detail.eventDetails)
                telemetryService.trackEvent("PersonArrived", event.telemetryProperties())
            }

            "accommodation.cas3.person.departed" -> {
                val person = personRepository.getByCrn(event.crn())
                val detail = cas3ApiClient.getPersonDeparted(event.url())
                contactService.createOrUpdateContact(event.crn(), person) {
                    detail
                }
                addressService.endMainCAS3Address(person, detail.eventDetails.departedAt)
                telemetryService.trackEvent("PersonDeparted", event.telemetryProperties())
            }

            "accommodation.cas3.person.arrived.updated" -> {
                val person = personRepository.getByCrn(event.crn())
                val detail = cas3ApiClient.getPersonArrived(event.url())
                contactService.createOrUpdateContact(
                    event.crn(),
                    replaceNotes = false,
                    extraInfo = "Address details were updated: ${DeliusDateTimeFormatter.format(detail.timestamp)}"
                ) { detail }
                addressService.updateCas3Address(person, detail.eventDetails)
                telemetryService.trackEvent("PersonArrivedUpdated", event.telemetryProperties())
            }

            "accommodation.cas3.person.departed.updated" -> {
                contactService.createOrUpdateContact(event.crn(), replaceNotes = false) {
                    cas3ApiClient.getPersonDeparted(event.url())
                }
                telemetryService.trackEvent("PersonDepartedUpdated", event.telemetryProperties())
            }

            "accommodation.cas3.booking.cancelled.updated" -> {
                contactService.createOrUpdateContact(event.crn(), replaceNotes = false) {
                    cas3ApiClient.getBookingCancelledDetails(event.url())
                }
                telemetryService.trackEvent("BookingCancelledUpdated", event.telemetryProperties())
            }

            else -> throw IllegalArgumentException("Unexpected event type ${event.eventType}")
        }
    }

    fun HmppsDomainEvent.telemetryProperties() = mapOf(
        "occurredAt" to occurredAt.toString(),
        "crn" to crn()
    )
}

fun HmppsDomainEvent.crn(): String = requireNotNull(personReference.findCrn()) { "Missing CRN" }

fun HmppsDomainEvent.url(): URI = URI.create(requireNotNull(detailUrl) { "Missing detail url" })

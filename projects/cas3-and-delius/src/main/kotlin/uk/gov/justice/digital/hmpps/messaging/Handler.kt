package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.Cas3Service
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Component
@Channel("cas3-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val cas3Service: Cas3Service,
    private val notifier: Notifier,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(
                title = "accommodation.cas3.referral.submitted",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                title = "accommodation.cas3.booking.cancelled",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                title = "accommodation.cas3.booking.confirmed",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                title = "accommodation.cas3.booking.provisionally-made",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                title = "accommodation.cas3.person.arrived",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                title = "accommodation.cas3.person.departed",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                title = "accommodation.cas3.person.arrived.updated",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                title = "accommodation.cas3.person.departed.updated",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                title = "accommodation.cas3.booking.cancelled.updated",
                payload = Schema(HmppsDomainEvent::class)
            ),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val event = notification.message
        when (event.eventType) {
            "accommodation.cas3.referral.submitted" -> {
                cas3Service.referralSubmitted(event)
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }

            "accommodation.cas3.booking.cancelled" -> {
                cas3Service.bookingCancelled(event)
                telemetryService.trackEvent("BookingCancelled", event.telemetryProperties())
            }

            "accommodation.cas3.booking.confirmed" -> {
                cas3Service.bookingConfirmed(event)
                telemetryService.trackEvent("BookingConfirmed", event.telemetryProperties())
            }

            "accommodation.cas3.booking.provisionally-made" -> {
                cas3Service.bookingProvisionallyMade(event)
                telemetryService.trackEvent("BookingProvisionallyMade", event.telemetryProperties())
            }

            "accommodation.cas3.person.arrived" -> {
                val (previousAddress, newAddress) = cas3Service.personArrived(event)
                notifier.addressCreated(event.crn(), newAddress.id, newAddress.status.description)
                previousAddress?.let { notifier.addressUpdated(event.crn(), it.id, it.status.description) }
                telemetryService.trackEvent("PersonArrived", event.telemetryProperties())
            }

            "accommodation.cas3.person.departed" -> {
                cas3Service.personDeparted(event)?.let { updatedAddress ->
                    notifier.addressUpdated(event.crn(), updatedAddress.id, updatedAddress.status.description)
                }
                telemetryService.trackEvent("PersonDeparted", event.telemetryProperties())
            }

            "accommodation.cas3.person.arrived.updated" -> {
                cas3Service.personArrivedUpdated(event)?.let { updatedAddress ->
                    notifier.addressUpdated(event.crn(), updatedAddress.id, updatedAddress.status.description)
                }
                telemetryService.trackEvent("PersonArrivedUpdated", event.telemetryProperties())
            }

            "accommodation.cas3.person.departed.updated" -> {
                cas3Service.personDepartedUpdated(event)
                telemetryService.trackEvent("PersonDepartedUpdated", event.telemetryProperties())
            }

            "accommodation.cas3.booking.cancelled.updated" -> {
                cas3Service.bookingCancelledUpdated(event)
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

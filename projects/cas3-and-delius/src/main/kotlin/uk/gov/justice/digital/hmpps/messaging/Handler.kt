package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.delius.ContactService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val contactService: ContactService
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val event = notification.message
        when (event.eventType) {
            "accommodation.cas3.referral.submitted" -> {
                contactService.createReferralSubmitted(event)
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }
            "accommodation.cas3.booking.cancelled" -> {
                contactService.createBookingCancelled(event)
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }
            "accommodation.cas3.booking.confirmed" -> {
                contactService.createBookingConfirmed(event)
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }
            "accommodation.cas3.booking.provisionally-made" -> {
                contactService.createBookingProvisionallyMade(event)
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
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

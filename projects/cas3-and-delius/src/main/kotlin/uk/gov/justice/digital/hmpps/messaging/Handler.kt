package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.delius.ContactService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val contactService: ContactService,
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val event = notification.message
        when (event.eventType) {
            "accommodation.cas3.referral.submitted" -> {
                contactService.createReferralSubmitted(event)
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }
            else -> throw IllegalArgumentException("Unexpected event type ${event.eventType}")
        }
    }

    fun HmppsDomainEvent.telemetryProperties() = mapOf(
        "occurredAt" to occurredAt.toString(),
        "crn" to crn(),
        "noms" to noms(),

    )

    fun HmppsDomainEvent.crn(): String = personReference.findCrn() ?: "N/A"
    fun HmppsDomainEvent.noms(): String = personReference.findNomsNumber() ?: "N/A"
}

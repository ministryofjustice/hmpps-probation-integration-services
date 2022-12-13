package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.ApprovedPremisesService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class Handler(
    private val telemetryService: TelemetryService,
    private val approvedPremisesService: ApprovedPremisesService,
    override val converter: NotificationConverter<HmppsDomainEvent>,
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)

        val event = notification.message
        when (event.eventType) {
            "approved-premises.application.submitted" -> {
                approvedPremisesService.applicationSubmitted(event)
                telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
            }
            else -> throw IllegalArgumentException("Unexpected event type ${event.eventType}")
        }
    }
}

fun HmppsDomainEvent.telemetryProperties() = mapOf(
    "occurredAt" to occurredAt.toString(),
    "crn" to (personReference.findCrn() ?: "Unknown"),
)

package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
class CvlHandler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val licenceActivatedHandler: LicenceActivatedHandler
) : NotificationHandler<HmppsDomainEvent> {

    override fun handle(notification: Notification<HmppsDomainEvent>) {
        val results = when (notification.eventType?.let { DomainEventType.of(it) }) {
            is DomainEventType.LicenceActivated -> licenceActivatedHandler.licenceActivated(notification.message)
            else -> listOf(
                ActionResult.Ignored("Unexpected Event Type"),
                mapOf("eventType" to (notification.eventType ?: ""))
            )
        }

        val failure = results.firstOrNull { it is ActionResult.Failure } as ActionResult.Failure?
        if (failure == null) {
            results.forEach {
                when (it) {
                    is ActionResult.Success -> telemetryService.trackEvent(it.type.name, it.properties)
                    is ActionResult.Ignored -> telemetryService.trackEvent(it.reason, it.properties)
                    else -> throw IllegalArgumentException("Unexpected Action Result: $it")
                }
            }
        } else {
            throw failure.exception
        }
    }
}

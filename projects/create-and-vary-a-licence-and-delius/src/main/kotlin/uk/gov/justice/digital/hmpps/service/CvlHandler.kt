package uk.gov.justice.digital.hmpps.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@ConditionalOnProperty("cvl.handler.active")
class CvlHandler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val licenceActivatedHandler: LicenceActivatedHandler,
    private val featureFlags: FeatureFlags
) : NotificationHandler<HmppsDomainEvent> {

    override fun handle(notification: Notification<HmppsDomainEvent>) {
        if (!featureFlags.enabled("cvl-licence-activated")) {
            return
        }
        val results =
            when (val eventType = (notification.eventType ?: notification.message.eventType).let { DomainEventType.of(it) }) {
                is DomainEventType.LicenceActivated -> licenceActivatedHandler.licenceActivated(notification.message)
                else -> listOf(
                    ActionResult.Ignored(
                        "UnexpectedEventType",
                        mapOf("eventType" to eventType.name)
                    )
                )
            }

        val failure = results.firstOrNull { it is ActionResult.Failure } as ActionResult.Failure?
        if (failure == null) {
            results.forEach {
                if (it is ActionResult.Success) {
                    telemetryService.trackEvent(it.type.name, it.properties)
                } else if (it is ActionResult.Ignored) {
                    telemetryService.trackEvent(it.reason, it.properties)
                }
            }
        } else {
            throw failure.exception
        }
    }
}

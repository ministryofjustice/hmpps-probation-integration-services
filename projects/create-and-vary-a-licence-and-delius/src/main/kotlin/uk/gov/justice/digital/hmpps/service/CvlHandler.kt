package uk.gov.justice.digital.hmpps.service

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import uk.gov.justice.digital.hmpps.service.DomainEventType.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("create-and-vary-a-licence-and-delius-queue")
class CvlHandler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val licenceActivatedHandler: LicenceActivatedHandler
) : NotificationHandler<HmppsDomainEvent> {

    @Publish(
        messages = [
            Message(name = "create-and-vary-a-licence/licence-activated"),
            Message(
                title = "create-and-vary-a-licence.prrd-licence.activated",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                title = "create-and-vary-a-licence.time-served-licence.activated",
                payload = Schema(HmppsDomainEvent::class)
            ),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        val eventType = (notification.eventType ?: notification.message.eventType).let { DomainEventType.of(it) }
        val results = when (eventType) {
            is LicenceActivated, PRRDLicenceActivated, TimeServedLicenceActivated ->
                licenceActivatedHandler.licenceActivated(notification.message)

            else -> listOf(ActionResult.Ignored("UnexpectedEventType", mapOf("eventType" to eventType.name)))
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

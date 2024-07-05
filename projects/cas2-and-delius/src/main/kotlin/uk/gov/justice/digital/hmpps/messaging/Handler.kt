package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.Cas2Service
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("cas2-and-delius-queue")
class Handler(
    @Value("\${event.exception.throw-not-found:true}") private val throwNotFound: Boolean,
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val cas2Service: Cas2Service,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(
                messageId = "applications.cas2.application.submitted",
                payload = Schema(HmppsDomainEvent::class)
            ),
            Message(
                messageId = "applications.cas2.application.status-updated",
                payload = Schema(HmppsDomainEvent::class)
            ),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val event = notification.message
        try {
            when (event.eventType) {
                "applications.cas2.application.submitted" -> cas2Service.applicationSubmitted(event)
                "applications.cas2.application.status-updated" -> cas2Service.applicationStatusUpdated(event)

                else -> throw IllegalArgumentException("Unexpected event type ('${event.eventType}')")
            }
        } catch (ex: HttpStatusCodeException) {
            if (ex.statusCode != HttpStatus.NOT_FOUND || throwNotFound) {
                throw ex
            }
        }
    }
}

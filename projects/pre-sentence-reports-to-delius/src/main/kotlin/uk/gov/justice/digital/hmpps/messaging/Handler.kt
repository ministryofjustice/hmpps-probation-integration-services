package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("pre-sentence-reports-to-delius-queue")
class Handler(
    private val telemetryService: TelemetryService,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(name = "pre-sentence/pre-sentence_report_completed")])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
    }
}

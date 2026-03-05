package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.DocumentService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.lang.IllegalArgumentException

@Component
@Channel("pre-sentence-reports-to-delius-queue")
class Handler(
    private val telemetryService: TelemetryService,
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val detailService: DomainEventDetailService,
    private val documentService: DocumentService,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(name = "pre-sentence.report.created", payload = Schema(HmppsDomainEvent::class))
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        when (notification.eventType) {
            "pre-sentence.report.created" -> {
                val file = detailService.getDetail<ByteArray>(notification.message)
                documentService.uploadDocument(notification.message, file)
                telemetryService.trackEvent("DocumentUploaded", notification.message.telemetry())
            }

            else -> {
                telemetryService.trackEvent("UnknownEventType", notification.message.telemetry())
            }
        }
    }
}

val HmppsDomainEvent.psrId
    get() = additionalInformation["psrId"].toString() ?: throw IllegalArgumentException("Missing psrId")
val HmppsDomainEvent.username
    get() = additionalInformation["username"].toString() ?: throw IllegalArgumentException("Missing username")

fun HmppsDomainEvent.telemetry() = mapOf(
    "crn" to personReference.findCrn(),
    "psrId" to psrId,
    "username" to username,
)

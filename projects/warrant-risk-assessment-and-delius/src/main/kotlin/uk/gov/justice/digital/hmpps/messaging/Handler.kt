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
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived

@Component
@Channel("warrant-risk-assessment-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val documentService: DocumentService,
    private val detailService: DomainEventDetailService,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(title = "probation-case.warrant-risk-assessment.created", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.warrant-risk-assessment.deleted", payload = Schema(HmppsDomainEvent::class))
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        when (notification.eventType) {
            "probation-case.warrant-risk-assessment.created" -> {
                val file = detailService.getDetail<ByteArray>(notification.message)
                documentService.uploadDocument(notification.message, file)
                telemetryService.trackEvent("DocumentUploaded", notification.message.telemetry())
            }

            "probation-case.warrant-risk-assessment.deleted" -> {
                documentService.deleteDocument(notification.message)
                telemetryService.trackEvent("DocumentDeleted", notification.message.telemetry())
            }
        }
    }
}

val HmppsDomainEvent.wraId get() = additionalInformation["WRAId"] as String
val HmppsDomainEvent.username get() = additionalInformation["username"] as String
fun HmppsDomainEvent.telemetry() = mapOf(
    "crn" to personReference.findCrn(),
    "WRAId" to wraId,
    "username" to username,
)

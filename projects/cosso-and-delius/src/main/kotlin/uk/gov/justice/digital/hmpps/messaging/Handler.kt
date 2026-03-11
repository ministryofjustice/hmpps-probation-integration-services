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
import java.lang.IllegalArgumentException

@Component
@Channel("cosso-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val detailService: DomainEventDetailService,
    private val documentService: DocumentService,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(name = "probation-case.COSSO.created", payload = Schema(HmppsDomainEvent::class))
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        when (notification.eventType) {
            "probation-case.COSSO.created" -> {
                val file = detailService.getDetail<ByteArray>(notification.message)
                documentService.uploadDocument(notification.message, file)
                telemetryService.trackEvent("DocumentUploaded", notification.message.telemetry())
            }
            "probation-case.COSSO.deleted" -> {
                documentService.deleteDocument(notification.message)
                telemetryService.trackEvent("DocumentDeleted", notification.message.telemetry())
            }
        }
    }
}

val HmppsDomainEvent.cossoBreachNoticeId
    get() = additionalInformation["COSSOBreachNoticeId"] as String? ?: throw IllegalArgumentException("Missing cossoBreachNoticeId")
val HmppsDomainEvent.username
    get() = additionalInformation["username"] as String? ?: throw IllegalArgumentException("Missing username")

fun HmppsDomainEvent.telemetry() = mapOf(
    "crn" to personReference.findCrn(),
    "cossoBreachNoticeId" to cossoBreachNoticeId,
    "username" to username,
)

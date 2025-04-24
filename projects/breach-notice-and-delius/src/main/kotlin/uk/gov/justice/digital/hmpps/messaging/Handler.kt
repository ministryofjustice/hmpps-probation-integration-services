package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.DocumentService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Transactional
@Channel("breach-notice-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val detailService: DomainEventDetailService,
    private val documentService: DocumentService,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(title = "probation-case.breach-notice.created", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.breach-notice.deleted", payload = Schema(HmppsDomainEvent::class))
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)

        when (notification.eventType) {
            "probation-case.breach-notice.created" -> {
                val file = detailService.getDetail<ByteArray>(notification.message)
                documentService.uploadDocument(notification.message, file)
                telemetryService.trackEvent("DocumentUploaded", notification.message.telemetry())
            }

            "probation-case.breach-notice.deleted" -> {
                documentService.deleteDocument(notification.message)
                telemetryService.trackEvent("DocumentDeleted", notification.message.telemetry())
            }
        }
    }
}

val HmppsDomainEvent.breachNoticeId get() = requireNotNull(additionalInformation["breachNoticeId"] as String?)
val HmppsDomainEvent.username get() = requireNotNull(additionalInformation["username"] as String?)
fun HmppsDomainEvent.telemetry() = mapOf(
    "crn" to personReference.findCrn(),
    "breachNoticeId" to additionalInformation["breachNoticeId"] as String?,
    "username" to additionalInformation["username"] as String?,
)
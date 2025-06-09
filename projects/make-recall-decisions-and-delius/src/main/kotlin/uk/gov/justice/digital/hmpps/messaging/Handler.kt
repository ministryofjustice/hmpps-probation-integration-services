package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.makerecalldecisions.RecommendationDetails
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.RecommendationService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Component
@Channel("make-recall-decisions-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val recommendationService: RecommendationService,
    private val detailService: DomainEventDetailService,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(name = "consider-a-recall/recommendation-consideration"),
            Message(name = "consider-a-recall/recommendation-management-oversight"),
            Message(name = "consider-a-recall/recommendation-deleted"),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) = try {
        telemetryService.notificationReceived(notification)
        val crn = notification.message.personReference.findCrn()
            ?: throw IllegalArgumentException("CRN not found in message")
        when (notification.eventType) {
            "prison-recall.recommendation.management-oversight" -> recommendationService.managementOversight(
                crn = crn,
                decision = notification.decision(),
                details = notification.details(),
                username = notification.bookedByUsername(),
                occurredAt = notification.message.occurredAt
            )

            "prison-recall.recommendation.deleted" -> recommendationService.deletion(
                crn = crn,
                details = notification.details(),
                username = notification.bookedByUsername(),
                occurredAt = notification.message.occurredAt
            )

            "prison-recall.recommendation.consideration" -> recommendationService.consideration(
                crn = crn,
                details = notification.details(),
                username = notification.bookedByUsername(),
                occurredAt = notification.message.occurredAt
            )

            else -> throw NotImplementedError("Unhandled message type received: ${notification.eventType}")
        }
    } catch (ime: IgnorableMessageException) {
        telemetryService.trackEvent(ime.message, ime.additionalProperties)
    }

    private fun Notification<HmppsDomainEvent>.details(): RecommendationDetails = detailService.getDetail(this.message)
}

private fun Notification<HmppsDomainEvent>.decision() =
    ManagementDecision.valueOf(message.additionalInformation["contactOutcome"] as String)

private fun Notification<HmppsDomainEvent>.bookedByUsername(): String =
    (message.additionalInformation["bookedBy"] as Map<*, *>)["username"] as String?
        ?: throw IllegalArgumentException("No Staff Code present in message")

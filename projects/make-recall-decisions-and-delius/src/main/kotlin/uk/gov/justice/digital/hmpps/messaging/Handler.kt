package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.makerecalldecisions.MakeRecallDecisionsClient
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
    private val makeRecallDecisionsClient: MakeRecallDecisionsClient,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(name = "consider-a-recall/management_oversight"),
            Message(title = "prison-recall.recommendation.deleted", payload = Schema(HmppsDomainEvent::class)),
            Message(
                title = "prison-recall.recommendation.consideration",
                payload = Schema(HmppsDomainEvent::class)
            ),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
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
    }

    private fun Notification<HmppsDomainEvent>.details() = makeRecallDecisionsClient.getDetails(detailUrl())
}

private fun Notification<HmppsDomainEvent>.detailUrl() =
    message.detailUrl?.takeIf { it.isNotBlank() }?.let { URI(it) }
        ?: throw IllegalArgumentException("No detail url provided")

private fun Notification<HmppsDomainEvent>.decision() =
    ManagementDecision.valueOf(message.additionalInformation["contactOutcome"] as String)

private fun Notification<HmppsDomainEvent>.bookedByUsername(): String =
    (message.additionalInformation["bookedBy"] as Map<*, *>)["username"] as String?
        ?: throw IllegalArgumentException("No Staff Code present in message")

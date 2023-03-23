package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.ManagementOversightRecall
import uk.gov.justice.digital.hmpps.service.RecommendationStarted
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val recommendationStarted: RecommendationStarted,
    private val managementOversightRecall: ManagementOversightRecall,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val crn = notification.message.personReference.findCrn()
            ?: throw IllegalArgumentException("CRN not found in message")
        when (notification.eventType) {
            "prison-recall.recommendation.started" -> recommendationStarted.recommended(
                crn,
                notification.recommendationUrl(),
                notification.message.occurredAt
            )
            "prison-recall.recommendation.managementOversight" -> managementOversightRecall.decision(
                crn,
                notification.recommendationUrl(),
                notification.message.occurredAt,
                notification.managementDecision(),
                notification.bookedByStaffCode()
            )
            else -> throw NotImplementedError("Unhandled message type received: ${notification.eventType}")
        }
    }
}

private fun Notification<HmppsDomainEvent>.recommendationUrl() =
    message.additionalInformation["recommendationUrl"] as String

private fun Notification<HmppsDomainEvent>.managementDecision() =
    ManagementDecision.valueOf(message.additionalInformation["contactOutcome"] as String)

private fun Notification<HmppsDomainEvent>.bookedByStaffCode(): String =
    (message.additionalInformation["bookedBy"] as Map<String, String>)["staffCode"]
        ?: throw IllegalArgumentException("No Staff Code present in message")

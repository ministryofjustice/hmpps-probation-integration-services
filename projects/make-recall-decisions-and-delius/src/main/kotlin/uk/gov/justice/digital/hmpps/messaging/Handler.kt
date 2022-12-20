package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.RecommendationStarted
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val recommendationStarted: RecommendationStarted,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        notification.message.personReference.findCrn()?.let {
            when (notification.eventType) {
                "prison-recall.recommendation.started" -> recommendationStarted.recommended(
                    it,
                    notification.recommendationUrl()
                )
                else -> throw NotImplementedError("Unhandled message type received: ${notification.eventType}")
            }
        }
    }

    private fun Notification<HmppsDomainEvent>.recommendationUrl() =
        message.additionalInformation["recommendationUrl"] as String
}

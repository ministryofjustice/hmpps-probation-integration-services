package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.GoalsService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("sentence-plan-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    val goalsService: GoalsService,
    val telemetryService: TelemetryService,
) : NotificationHandler<HmppsDomainEvent> {

    @Publish(
        messages = [
            Message(title = GOALS_COMPLETED),
            Message(title = GOALS_ADDED),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        when (notification.eventType) {
            GOALS_COMPLETED -> {
                goalsService.goalsAchieved(notification.message)
                telemetryService.trackEvent("GoalsAchieved", notification.telemetry())
            }

            GOALS_ADDED -> {
                goalsService.goalsAchievedRemoved(notification.message)
                telemetryService.trackEvent("GoalsAchievedRemoved", notification.telemetry())
            }

            else -> throw IllegalArgumentException("Unexpected event type: ${notification.eventType}")
        }
    }

    companion object {
        const val GOALS_COMPLETED = "arns.sentence.plan.goals.completed"
        const val GOALS_ADDED = "arns.sentence.plan.goals.added"
    }
}

val HmppsDomainEvent.crn get() = requireNotNull(personReference.findCrn())

fun Notification<HmppsDomainEvent>.telemetry() = mapOf(
    "eventType" to eventType,
    "crn" to message.crn,
)

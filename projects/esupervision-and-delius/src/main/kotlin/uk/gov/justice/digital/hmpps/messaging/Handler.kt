package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_EXPIRED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_RECEIVED
import uk.gov.justice.digital.hmpps.service.CheckInService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("esupervision-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    val checkInService: CheckInService,
    val telemetryService: TelemetryService,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(title = CHECK_IN_RECEIVED),
            Message(title = CHECK_IN_EXPIRED),
            Message(title = CHECK_IN_REVIEWED),
            Message(title = CHECK_IN_UPDATED),
            Message(title = SETUP_COMPLETED),
            Message(title = SETUP_REMOVED),
            Message(title = SENTENCE_TERMINATED),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        try {
            when (notification.eventType) {
                CHECK_IN_RECEIVED, CHECK_IN_EXPIRED -> {
                    checkInService.receiveCheckIn(notification.message)
                    telemetryService.trackEvent("CheckInEventReceived", notification.telemetry())
                }

                CHECK_IN_REVIEWED, CHECK_IN_UPDATED -> {
                    checkInService.updateCheckIn(notification.message)
                    telemetryService.trackEvent("CheckInEventUpdated", notification.telemetry())
                }

                SETUP_COMPLETED -> {
                    checkInService.completeSetup(notification.message)
                    telemetryService.trackEvent("CheckInSetupCompleted", notification.telemetry())
                }

                SETUP_REMOVED, SENTENCE_TERMINATED -> {
                    checkInService.removeSetup(notification.message)
                    telemetryService.trackEvent("CheckInSetupRemoved", notification.telemetry())
                }

                else -> throw IllegalArgumentException("Unexpected event type: ${notification.eventType}")
            }
        } catch (e: IgnorableMessageException) {
            telemetryService.trackEvent(
                "CheckInEventIgnored",
                mapOf("reason" to e.message) + e.additionalProperties + notification.telemetry()
            )
        }
    }

    companion object {
        const val CHECK_IN_RECEIVED = "esupervision.check-in.received"
        const val CHECK_IN_EXPIRED = "esupervision.check-in.expired"
        const val CHECK_IN_REVIEWED = "esupervision.check-in.reviewed"
        const val CHECK_IN_UPDATED = "esupervision.check-in.updated"
        const val SETUP_COMPLETED = "esupervision.setup.completed"
        const val SETUP_REMOVED = "esupervision.setup.removed"
        const val SENTENCE_TERMINATED = "probation-case.sentence.terminated"
    }
}

fun Notification<HmppsDomainEvent>.telemetry() = mapOf(
    "eventType" to eventType,
    "crn" to message.crn,
    "eventNumber" to message.eventNumber,
    "checkInUrl" to message.checkInUrl,
    "setupId" to message.setupId,
)

fun HmppsDomainEvent.description() = when (eventType) {
    CHECK_IN_RECEIVED -> "Online check in completed"
    CHECK_IN_EXPIRED -> "Check in has not been submitted on time"
    else -> throw IllegalArgumentException("Unexpected event type: $eventType")
}

val HmppsDomainEvent.crn get() = requireNotNull(personReference.findCrn())
val HmppsDomainEvent.checkInUrl get() = additionalInformation["checkInUrl"]?.toString()
val HmppsDomainEvent.eventNumber get() = additionalInformation["eventNumber"]?.toString()
val HmppsDomainEvent.setupId get() = additionalInformation["setupId"]?.toString()

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
            Message(title = CHECK_IN_UPDATED)
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        try {
            when (notification.eventType) {
                CHECK_IN_RECEIVED, CHECK_IN_EXPIRED -> {
                    checkInService.handle(notification.message)
                    telemetryService.trackEvent("CheckInEventReceived", notification.telemetry())
                }

                CHECK_IN_REVIEWED, CHECK_IN_UPDATED -> {
                    checkInService.update(notification.message)
                    telemetryService.trackEvent("CheckInEventUpdated", notification.telemetry())
                }

                else -> throw IllegalArgumentException("Unexpected event type: ${notification.eventType}")
            }
        } catch (ie: IgnorableMessageException) {
            telemetryService.trackEvent("CheckInEventIgnored", notification.telemetry() + ie.additionalProperties)
        }
    }

    companion object {
        const val CHECK_IN_RECEIVED = "esupervision.check-in.received"
        const val CHECK_IN_EXPIRED = "esupervision.check-in.expired"
        const val CHECK_IN_REVIEWED = "esupervision.check-in.reviewed"
        const val CHECK_IN_UPDATED = "esupervision.check-in.updated"
    }
}

fun Notification<HmppsDomainEvent>.telemetry() = mapOf(
    "eventType" to eventType,
    "crn" to message.personReference.findCrn(),
)

fun HmppsDomainEvent.description() = when (eventType) {
    CHECK_IN_RECEIVED -> "Online check in completed"
    CHECK_IN_EXPIRED -> "Check in has not been submitted on time"
    else -> throw IllegalArgumentException("Unexpected event type: $eventType")
}

fun HmppsDomainEvent.checkInUrl() = additionalInformation["checkInUrl"]?.toString()

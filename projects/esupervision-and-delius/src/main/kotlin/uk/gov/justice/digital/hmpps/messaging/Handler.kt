package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
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
    @Publish(messages = [Message(title = CHECK_IN_RECEIVED), Message(title = CHECK_IN_EXPIRED)])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        when (notification.eventType) {
            CHECK_IN_RECEIVED, CHECK_IN_EXPIRED -> {
                checkInService.handle(notification.message)
                telemetryService.trackEvent(
                    "CheckInEventReceived", mapOf(
                        "eventType" to notification.eventType,
                        "crn" to notification.message.personReference.findCrn(),
                    )
                )
            }

            else -> throw IllegalArgumentException("Unexpected event type: ${notification.eventType}")
        }
    }

    companion object {
        const val CHECK_IN_RECEIVED = "esupervision.check-in.received"
        const val CHECK_IN_EXPIRED = "esupervision.check-in.expired"
    }
}

fun HmppsDomainEvent.description() = when (eventType) {
    CHECK_IN_RECEIVED -> "Online check in completed"
    CHECK_IN_EXPIRED -> "Check in has not been submitted on time"
    else -> throw IllegalArgumentException("Unexpected event type: $eventType")
}

fun HmppsDomainEvent.checkInUrl() = additionalInformation["checkInUrl"]?.toString()

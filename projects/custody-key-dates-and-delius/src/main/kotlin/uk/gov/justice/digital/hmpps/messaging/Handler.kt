package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateUpdateService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
@Channel("custody-key-dates-and-delius-queue")
class Handler(
    override val converter: KeyDateChangedEventConverter,
    private val cduService: CustodyDateUpdateService,
    private val telemetryService: TelemetryService
) : NotificationHandler<Any> {
    @Publish(
        messages = [
            Message(messageId = "probation-case.prison-identifier.added", payload = Schema(HmppsDomainEvent::class)),
            Message(messageId = "probation-case.prison-identifier.updated", payload = Schema(HmppsDomainEvent::class)),
            Message(messageId = "SENTENCE_DATES-CHANGED", payload = Schema(CustodyDateChanged::class)),
            Message(messageId = "CONFIRMED_RELEASE_DATE-CHANGED", payload = Schema(CustodyDateChanged::class)),
            Message(messageId = "KEY_DATE_ADJUSTMENT_UPSERTED", payload = Schema(CustodyDateChanged::class)),
            Message(messageId = "KEY_DATE_ADJUSTMENT_DELETED", payload = Schema(CustodyDateChanged::class)),
        ]
    )
    override fun handle(notification: Notification<Any>) {
        telemetryService.notificationReceived(notification)
        when (val message = notification.message) {
            is HmppsDomainEvent -> message.personReference.findNomsNumber()
                ?.let { cduService.updateCustodyKeyDates(it) }

            is CustodyDateChanged -> cduService.updateCustodyKeyDates(message.bookingId)
            is ProbationOffenderEvent -> when (notification.eventType) {
                "SENTENCE_CHANGED",
                -> cduService.updateCustodyKeyDates(cduService.findNomsByCrn(message.crn))

                else -> throw IllegalArgumentException("Unexpected offender event type: ${notification.eventType}")
            }
        }
    }
}

@Message
data class CustodyDateChanged(val bookingId: Long)
data class ProbationOffenderEvent(val crn: String)

@Primary
@Component
class KeyDateChangedEventConverter(objectMapper: ObjectMapper) : NotificationConverter<Any>(objectMapper) {
    override fun getMessageType() = Any::class

    override fun fromMessage(message: String): Notification<Any> {
        val stringMessage = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>())
        val json = objectMapper.readTree(stringMessage.message)
        if (json.has("bookingId")) {
            return Notification(
                message = objectMapper.readValue(stringMessage.message, CustodyDateChanged::class.java),
                attributes = stringMessage.attributes
            )
        }
        if (json.has("crn")) {
            return Notification(
                message = objectMapper.readValue(stringMessage.message, ProbationOffenderEvent::class.java),
                attributes = stringMessage.attributes
            )
        }
        return Notification(
            message = objectMapper.readValue(stringMessage.message, HmppsDomainEvent::class.java),
            attributes = stringMessage.attributes
        )
    }
}

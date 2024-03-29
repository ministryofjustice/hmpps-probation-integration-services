package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateUpdateService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class Handler(
    override val converter: KeyDateChangedEventConverter,
    private val cduService: CustodyDateUpdateService,
    private val telemetryService: TelemetryService
) : NotificationHandler<Any> {
    override fun handle(notification: Notification<Any>) {
        telemetryService.notificationReceived(notification)
        when (val message = notification.message) {
            is HmppsDomainEvent -> message.personReference.findNomsNumber()
                ?.let { cduService.updateCustodyKeyDates(it) }

            is CustodyDateChanged -> cduService.updateCustodyKeyDates(message.bookingId)
        }
    }
}

data class CustodyDateChanged(val bookingId: Long)

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
        return Notification(
            message = objectMapper.readValue(stringMessage.message, HmppsDomainEvent::class.java),
            attributes = stringMessage.attributes
        )
    }
}

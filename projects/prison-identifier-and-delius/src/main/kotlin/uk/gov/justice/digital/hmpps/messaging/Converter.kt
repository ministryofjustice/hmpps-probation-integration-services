package uk.gov.justice.digital.hmpps.messaging

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification

@Primary
@Component
class Converter(objectMapper: ObjectMapper) : NotificationConverter<Any>(objectMapper) {
    override fun getMessageType() = Any::class

    override fun fromMessage(message: String): Notification<Any> {
        val stringMessage = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>())
        val type = if (objectMapper.readTree(stringMessage.message).has("crn"))
            OffenderEvent::class.java else HmppsDomainEvent::class.java

        return Notification(objectMapper.readValue(stringMessage.message, type), stringMessage.attributes)
    }
}

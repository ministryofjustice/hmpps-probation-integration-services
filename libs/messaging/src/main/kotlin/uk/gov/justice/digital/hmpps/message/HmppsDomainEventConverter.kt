package uk.gov.justice.digital.hmpps.message

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class HmppsDomainEventConverter(objectMapper: ObjectMapper) : NotificationConverter<HmppsDomainEvent>(objectMapper) {
    override fun getMessageClass(notification: Notification<*>) = HmppsDomainEvent::class
}

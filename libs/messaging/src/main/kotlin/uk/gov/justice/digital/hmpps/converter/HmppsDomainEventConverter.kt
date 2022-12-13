package uk.gov.justice.digital.hmpps.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent

@Component
class HmppsDomainEventConverter(objectMapper: ObjectMapper) : NotificationConverter<HmppsDomainEvent>(objectMapper) {
    override fun getMessageType() = HmppsDomainEvent::class
}

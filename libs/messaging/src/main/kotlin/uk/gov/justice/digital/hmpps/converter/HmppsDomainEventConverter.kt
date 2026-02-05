package uk.gov.justice.digital.hmpps.converter

import tools.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent

@Component
class HmppsDomainEventConverter(objectMapper: ObjectMapper) : NotificationConverter<HmppsDomainEvent>(objectMapper) {
    override fun getMessageType() = HmppsDomainEvent::class
}

package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter

@Primary
@Component
class Converter(objectMapper: ObjectMapper) : NotificationConverter<EmailMessage>(objectMapper) {
    override fun getMessageType() = EmailMessage::class
}
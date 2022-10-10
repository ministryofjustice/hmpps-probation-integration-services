package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.NotificationConverter

@Primary
@Component
class MessageConverter(objectMapper: ObjectMapper) : NotificationConverter<TierChangeEvent>(objectMapper) {
    override fun getMessageClass(notification: Notification<*>) = TierChangeEvent::class
}

data class TierChangeEvent(
    val crn: String,
    val calculationId: String
)

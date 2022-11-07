package uk.gov.justice.digital.hmpps.integrations.delius.offender

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.NotificationConverter

@Primary
@Component
class OffenderEventConverter(objectMapper: ObjectMapper) : NotificationConverter<OffenderEvent>(objectMapper) {
    override fun getMessageClass(notification: Notification<*>) = OffenderEvent::class
}

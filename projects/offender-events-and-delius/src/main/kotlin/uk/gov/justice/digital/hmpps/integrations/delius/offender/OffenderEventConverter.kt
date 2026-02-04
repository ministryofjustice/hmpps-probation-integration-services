package uk.gov.justice.digital.hmpps.integrations.delius.offender

import tools.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter

@Primary
@Component
class OffenderEventConverter(objectMapper: ObjectMapper) : NotificationConverter<OffenderEvent>(objectMapper) {
    override fun getMessageType() = OffenderEvent::class
}

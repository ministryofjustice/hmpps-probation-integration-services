package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonOffenderEvent
import uk.gov.justice.digital.hmpps.message.HmppsMessage
import uk.gov.justice.digital.hmpps.message.IntegrationEventConverter

@Primary
@Component
class CaseNoteConverter(om: ObjectMapper) : IntegrationEventConverter<PrisonOffenderEvent>(om) {
    override fun getEventClass(message: HmppsMessage) = PrisonOffenderEvent::class
}

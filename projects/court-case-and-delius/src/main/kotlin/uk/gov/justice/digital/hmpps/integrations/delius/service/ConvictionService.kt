package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.conviction.Conviction
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository

@Service
class ConvictionService(private val eventRepository: EventRepository) {
    fun getConvictionFor(offenderId: Long, eventId: Long): Conviction? {
        val event = eventRepository.findById(eventId)

        event.filter { e -> offenderId == e.person.id }
        return null
    }
}


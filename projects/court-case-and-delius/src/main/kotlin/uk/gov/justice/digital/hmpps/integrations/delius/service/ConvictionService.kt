package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.conviction.Conviction
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson

@Service
class ConvictionService(private val personRepository: PersonRepository, private val eventRepository: EventRepository) {
    fun getConvictionFor(crn: String, eventId: Long): Conviction? {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.findByPersonAndId(person, eventId)

        return event?.toConviction()
            ?: throw NotFoundException("Conviction with ID $eventId for Offender with crn $crn not found")
    }

    fun Event.toConviction(): Conviction = Conviction(id, eventNumber, active, inBreach, convictionDate)
}


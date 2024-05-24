package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.conviction.Conviction
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByPersonAndEventNumber
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson

@Service
class ConvictionService(private val personRepository: PersonRepository, private val eventRepository: EventRepository) {
    fun getConvictionFor(crn: String, eventId: Long): Conviction? {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getByPersonAndEventNumber(person, eventId)

        return event.toConviction()
    }

    fun Event.toConviction(): Conviction =
        Conviction(id, eventNumber, active, inBreach, failureToComplyCount, breachEnd, convictionDate, referralDate)
}


package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@Service
class EventService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
) {
    fun getActiveCustodialEvents(nomsNumber: String): List<Event> {
        val persons = personRepository.findByNomsNumberAndSoftDeletedIsFalse(nomsNumber)
        if (persons.isEmpty()) throw IgnorableMessageException("MissingNomsNumber")
        if (persons.size > 1) throw IgnorableMessageException("DuplicateNomsNumber")

        val events = eventRepository.findActiveCustodialEvents(persons.single().id)
        if (events.isEmpty()) throw IgnorableMessageException("NoActiveCustodialEvent")
        if (events.size > 1) throw IgnorableMessageException("MultipleActiveCustodialEvents") // This behaviour may change - see https://dsdmoj.atlassian.net/browse/PI-262

        return events
    }
}

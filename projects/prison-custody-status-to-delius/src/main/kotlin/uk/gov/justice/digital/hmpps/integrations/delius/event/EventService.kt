package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import java.time.ZonedDateTime

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
        return events
    }

    fun updateReleaseDateAndIapsFlag(
        event: Event,
        releaseDate: ZonedDateTime,
    ) {
        if (event.firstReleaseDate == null) {
            event.firstReleaseDate = releaseDate
            eventRepository.save(event)
        }
        eventRepository.updateIaps(event.id)
    }
}

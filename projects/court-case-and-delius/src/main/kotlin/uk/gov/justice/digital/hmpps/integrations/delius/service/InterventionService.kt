package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.NsiDetails
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByPersonAndEventNumber
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson

@Service
class InterventionService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository
) {

    fun getNsiByCodes(crn: String, convictionId: Long, nsiCodes: Collection<String>): NsiDetails {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getByPersonAndEventNumber(person, convictionId)

        return NsiDetails(emptyList())
    }
}
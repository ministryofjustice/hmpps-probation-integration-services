package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*

@Service
class OrdersService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventSentenceRepository,
    private val sentenceService: SentenceService
) {

    fun getPreviousEvents(crn: String): PreviousOrderHistory {
        val person = personRepository.getPerson(crn)
        val events = eventRepository.findSentencesByPersonId(person.id).filter { it.active.not() }

        return PreviousOrderHistory(name = person.toName(), events.map { it.toPreviousOrder() })
    }

    fun getPreviousEvent(crn: String, eventNumber: String): PreviousOrderInformation {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.findEventByPersonIdAndEventNumber(person.id, eventNumber).takeIf { it?.active == false }

        return PreviousOrderInformation(person.toName(), event?.setTitle() , sentenceService.getInactiveEvent(event))
    }

    fun Person.toName() =
        Name(forename, secondName, surname)

    fun Event.toPreviousOrder(): PreviousOrder = PreviousOrder(
        "${disposal?.type?.description} (${disposal?.length} ${disposal?.lengthUnit?.description})",
        mainOffence?.offence?.description,
        disposal?.terminationDate
    )

    fun Event.setTitle(): String? {
        return "${disposal?.type?.description} (${disposal?.length} ${disposal?.lengthUnit?.description})"
    }

}
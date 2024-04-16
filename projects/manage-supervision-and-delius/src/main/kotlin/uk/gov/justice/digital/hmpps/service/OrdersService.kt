package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.api.model.sentence.PreviousOrder
import uk.gov.justice.digital.hmpps.api.model.sentence.PreviousOrderHistory
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository

class OrdersService(private val personRepository: PersonRepository,
                    private val eventRepository: EventSentenceRepository) {

    fun getPreviousEvents(crn: String): PreviousOrderHistory {
        val person = personRepository.getPerson(crn)
        val events = eventRepository.findSentencesByPersonId(person.id).filter { it.active.not() }

        return PreviousOrderHistory(events.map { it.toPrevousOrder() })
    }

    fun Event.toPrevousOrder(): PreviousOrder = PreviousOrder(
        "${mainOffence?.offence?.description} (${disposal?.length} ${disposal?.lengthUnit?.description})",
        disposal?.type?.description
    )
}
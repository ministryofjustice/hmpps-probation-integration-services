package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.sentence.MainOffence
import uk.gov.justice.digital.hmpps.api.model.sentence.Offence
import uk.gov.justice.digital.hmpps.api.model.sentence.SentenceOverview
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonOverviewRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson

@Service
class SentenceService(
    private val personRepository: PersonOverviewRepository,
    private val eventRepository: EventSentenceRepository
) {
    fun getMostRecentActiveEvent(crn: String): SentenceOverview  {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.findPersonById(person.id)
            .filter { it.inBreach }
            .filter { it.active }
            .sortedByDescending { it.eventNumber }.elementAtOrNull(0)

        return SentenceOverview(event?.toOffence())
    }

    fun Event.toOffence() = mainOffence?.let { mainOffence ->
        MainOffence(offence = Offence(
            mainOffence.offence.description, mainOffence.offenceCount
        ),
            dateOfOffence = mainOffence.date,
            notes = notes,
            additionalOffences = additionalOffences.map {
                Offence(description = it.offence.description, count = it.offenceCount
                )
            }
        )
    }
}
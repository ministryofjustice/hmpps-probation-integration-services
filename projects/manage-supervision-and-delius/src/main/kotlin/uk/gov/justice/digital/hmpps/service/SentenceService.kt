package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.sentence.OffenceDetails
import uk.gov.justice.digital.hmpps.api.model.sentence.Offence
import uk.gov.justice.digital.hmpps.api.model.sentence.SentenceOverview
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository

@Service
class SentenceService(
    private val eventRepository: EventSentenceRepository
) {
    fun getMostRecentActiveEvent(crn: String): SentenceOverview {
        val events = eventRepository.findActiveSentencesByCrn(crn)

        return SentenceOverview(events.map { it.toOffence() })
    }

    fun Event.toOffence() = mainOffence?.let { mainOffence ->
        OffenceDetails(offence = Offence(
            mainOffence.offence.description, mainOffence.offenceCount
        ),
            dateOfOffence = mainOffence.date,
            notes = notes,
            additionalOffences = additionalOffences.map {
                Offence(
                    description = it.offence.description, count = it.offenceCount ?: 0
                )
            }
        )
    }
}
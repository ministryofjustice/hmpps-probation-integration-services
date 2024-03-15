package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository

@Service
class SentenceService(
    private val eventRepository: EventSentenceRepository,
    private val courtApperanceRepository: CourtAppearanceRepository
) {
    fun getMostRecentActiveEvent(crn: String): SentenceOverview {
        val events = eventRepository.findActiveSentencesByCrn(crn)
        return SentenceOverview(events.map {
            val courtApperance = courtApperanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(it.id)
            it.toSentence(courtApperance)
        })
    }

    fun Event.toSentence(courtAppearance: CourtAppearance?) = mainOffence?.let {
        mainOffence ->
        Sentence(
            (OffenceDetails(offence = Offence(mainOffence.offence.description, mainOffence.offenceCount),
                            dateOfOffence = mainOffence.date,
                            notes = notes,
                            additionalOffences = additionalOffences.map {
                                Offence(description = it.offence.description, count = it.offenceCount ?: 0)
                            }
                )
            ),
            Conviction(sentencingCourt = courtAppearance?.court?.name, responsibleCourt = court?.name, convictionDate = convictionDate)
        )
    }

}
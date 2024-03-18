package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentence as ExtraSentence


@Service
class SentenceService(
    private val eventRepository: EventSentenceRepository,
    private val courtApperanceRepository: CourtAppearanceRepository,
    private val additionalSentenceRepository: AdditionalSentenceRepository
) {
    fun getMostRecentActiveEvent(crn: String): SentenceOverview {
        val events = eventRepository.findActiveSentencesByCrn(crn)
        return SentenceOverview(events.map {
            val courtAppearance = courtApperanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(it.id)
            val additionalSentences = additionalSentenceRepository.getAllByEvent_Id(it.id)
            it.toSentence(courtAppearance, additionalSentences)
        })
    }

    fun Event.toSentence(courtAppearance: CourtAppearance?, additionalSentences: List<ExtraSentence>) = mainOffence?.let {
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
            Conviction(sentencingCourt = courtAppearance?.court?.name, responsibleCourt = court?.name, convictionDate = convictionDate),
            additionalSentences.map { it.toAdditionalSentence() }
        )
    }


    fun ExtraSentence.toAdditionalSentence(): AdditionalSentence =
        AdditionalSentence(length, amount, notes, refData!!.description)

}
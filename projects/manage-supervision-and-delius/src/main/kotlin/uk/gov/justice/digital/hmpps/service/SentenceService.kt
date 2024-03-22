package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonSummaryEntity
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getSummary
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentence as ExtraSentence

@Service
class SentenceService(
    private val eventRepository: EventSentenceRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
    private val additionalSentenceRepository: AdditionalSentenceRepository,
    private val personRepository: PersonRepository
) {
    fun getMostRecentActiveEvent(crn: String): SentenceOverview {
        val person = personRepository.getSummary(crn)
        val events = eventRepository.findActiveSentencesByPersonId(person.id)
        return SentenceOverview(
            name = person.toName(),
            sentences = events.map {
            val courtAppearance = courtAppearanceRepository.getFirstCourtAppearanceByEventIdOrderByDate(it.id)
            val additionalSentences = additionalSentenceRepository.getAllByEventId(it.id)
            it.toSentence(courtAppearance, additionalSentences)
        })
    }

    fun Event.toSentence(courtAppearance: CourtAppearance?, additionalSentences: List<ExtraSentence>) = Sentence(
        OffenceDetails(
            eventNumber = eventNumber,
            offence = mainOffence?.let { Offence(it.offence.description, it.offenceCount) },
            dateOfOffence = mainOffence?.date,
            notes = notes,
            additionalOffences = additionalOffences.map {
                Offence(description = it.offence.description, count = it.offenceCount ?: 0)
            }
        ),
        Conviction(
            sentencingCourt = courtAppearance?.court?.name,
            responsibleCourt = court?.name,
            convictionDate = convictionDate,
            additionalSentences.map { it.toAdditionalSentence() }
        )
    )

    fun ExtraSentence.toAdditionalSentence(): AdditionalSentence =
        AdditionalSentence(length, amount, notes, type.description)

    fun PersonSummaryEntity.toName() =
        Name(forename, secondName , surname)
}
package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.conviction.Conviction
import uk.gov.justice.digital.hmpps.api.model.conviction.Offence
import uk.gov.justice.digital.hmpps.api.model.conviction.OffenceDetail
import uk.gov.justice.digital.hmpps.api.model.conviction.Sentence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Offence as OffenceEntity

@Service
class ConvictionService(private val personRepository: PersonRepository, private val eventRepository: EventRepository) {
    fun getConvictionFor(crn: String, eventId: Long): Conviction? {
        val person = personRepository.getPerson(crn)
        val event = eventRepository.getByPersonAndEventNumber(person, eventId)

        return event.toConviction()
    }

    fun Event.toConviction(): Conviction =
        Conviction(
            id,
            eventNumber,
            active,
            inBreach,
            failureToComplyCount,
            breachEnd,
            eventRepository.awaitingPSR(id) == 1,
            convictionDate,
            referralDate,
            toOffences(),
            disposal?.toSentence()
        )

    fun Event.toOffences(): List<Offence> {
        val mainOffence = listOf(mainOffence!!.toOffence())
        val additionalOffences = additionalOffences.map { it.toOffence() }
        return mainOffence + additionalOffences
    }

    fun MainOffence.toOffence(): Offence =
        Offence(
            id,
            mainOffence = true,
            detail = offence.toOffenceDetail(),
            offenceDate = date,
            offenceCount = offenceCount,
            tics = tics,
            verdict = verdict,
            offenderId = event.person.id,
            createdDatetime = created,
            lastUpdatedDatetime = updated
        )

    fun AdditionalOffence.toOffence(): Offence =
        Offence(
            id,
            mainOffence = false,
            detail = offence.toOffenceDetail(),
            offenceDate = date,
            offenceCount = offenceCount,
            tics = null,
            verdict = null,
            offenderId = event.person.id,
            createdDatetime = created,
            lastUpdatedDatetime = updated
        )

    fun OffenceEntity.toOffenceDetail(): OffenceDetail =
        OffenceDetail(
            code,
            description,
            abbreviation,
            mainCategoryCode,
            mainCategoryDescription,
            mainCategoryAbbreviation,
            ogrsOffenceCategory.description,
            subCategoryCode,
            subCategoryDescription,
            form20Code,
            subCategoryAbbreviation,
            cjitCode
        )

    fun Disposal.toSentence(): Sentence =
        Sentence(
            id,
            disposalType.description,
            entryLength,
            entryLengthUnit?.description,
            length2,
            entryLength2Unit?.description,
            length,
            effectiveLength,
            lengthInDays,
            enteredSentenceEndDate,
            startDate,
            terminationDate,
            terminationReason?.description,
            KeyValue(disposalType.sentenceType, disposalType.description)
        )
}


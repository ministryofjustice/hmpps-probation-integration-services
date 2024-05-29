package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.conviction.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.AdditionalSentenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.UpwAppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.UpwDetails
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Offence as OffenceEntity
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.AdditionalSentence as AdditionalSentenceEntity

@Service
class ConvictionService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val upwAppointmentRepository: UpwAppointmentRepository,
    private val additionalSentenceRepository: AdditionalSentenceRepository
) {
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
            disposal?.toSentence(id),
            toLatestCourtAppearanceOutcome()
        )

    fun Event.toOffences(): List<Offence> {
        val mainOffence = listOf(mainOffence!!.toOffence())
        val additionalOffences = additionalOffences.map { it.toOffence() }
        return mainOffence + additionalOffences
    }

    fun Event.toLatestCourtAppearanceOutcome(): KeyValue? {
        courtAppearances.maxByOrNull { it.appearanceDate }
            ?.let { return KeyValue(it.outcome.code, it.outcome.description) }
            ?: return null
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

    fun Disposal.toSentence(eventId: Long): Sentence =
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
            unpaidWorkDetails?.toUnpaidWork(id),
            startDate,
            terminationDate,
            terminationReason?.description,
            KeyValue(disposalType.sentenceType, disposalType.description),
            additionalSentenceRepository.getAllByEventId(eventId).map { it.toAdditionalSentence() },
            disposalType.failureToComplyLimit,
            disposalType.cja2003Order,
            disposalType.legacyOrder
        )

    fun UpwDetails.toUnpaidWork(disposalId: Long): UnpaidWork {
        val unpaidWorkItems = upwAppointmentRepository.getUnpaidTimeWorked(disposalId)
        val minutesCompleted = unpaidWorkItems.find { it.type == "sum_minutes" }!!.value
        val totalAppointments = unpaidWorkItems.find { it.type == "total_appointments" }!!.value
        val attended = unpaidWorkItems.find { it.type == "attended" }!!.value
        val acceptableAbsence = unpaidWorkItems.find { it.type == "acceptable_absence" }!!.value
        val unacceptableAbsence = unpaidWorkItems.find { it.type == "unacceptable_absence" }!!.value
        val noOutcomeRecorded = unpaidWorkItems.find { it.type == "no_outcome_recorded" }!!.value

        return UnpaidWork(
            upwLengthMinutes,
            minutesCompleted,
            Appointments(totalAppointments, attended, acceptableAbsence, unacceptableAbsence, noOutcomeRecorded),
            status.description
        )
    }

    fun AdditionalSentenceEntity.toAdditionalSentence(): AdditionalSentence =
        AdditionalSentence(id, KeyValue(type.code, type.description), amount, length, notes)
}


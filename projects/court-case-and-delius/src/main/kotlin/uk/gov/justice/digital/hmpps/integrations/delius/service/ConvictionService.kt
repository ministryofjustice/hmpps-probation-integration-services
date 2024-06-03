package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.conviction.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Offence as OffenceEntity
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.AdditionalSentence as AdditionalSentenceEntity
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Custody as CustodyEntity
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Institution as InstitutionEntity

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
            toLatestCourtAppearanceOutcome(),
            disposal?.custody?.toCustody()
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

    fun CustodyEntity.toCustody(): Custody =
        Custody(
            prisonerNumber,
            institution.toInstitution(),
            populateKeyDates(keyDates),
            KeyValue(status.code, status.description),
            disposal.startDate
        )

    fun InstitutionEntity.toInstitution(): Institution =
        Institution(
            id.institutionId,
            id.establishment,
            code, description,
            institutionName,
            KeyValue(establishmentType.code, establishmentType.description),
            private,
            nomisCdeCode
        )

    fun populateKeyDates(keyDates: List<KeyDate>): CustodyRelatedKeyDates {
        val conditionalReleaseDate =
            keyDates.firstOrNull { it.keyDateType.code == KeyDateTypes.AUTOMATIC_CONDITIONAL_RELEASE_DATE.code }
        val licenceExpiryDate = keyDates.firstOrNull { it.keyDateType.code == KeyDateTypes.LICENCE_EXPIRY_DATE.code }
        val hdcEligibilityDate = keyDates.firstOrNull { it.keyDateType.code == KeyDateTypes.HDC_EXPECTED_DATE.code }
        val paroleEligibilityDate =
            keyDates.firstOrNull { it.keyDateType.code == KeyDateTypes.PAROLE_ELIGIBILITY_DATE.code }
        val sentenceExpiryDate = keyDates.firstOrNull { it.keyDateType.code == KeyDateTypes.SENTENCE_EXPIRY_DATE.code }
        val expectedReleaseDate =
            keyDates.firstOrNull { it.keyDateType.code == KeyDateTypes.EXPECTED_RELEASE_DATE.code }
        val postSentenceSupervisionEndDate =
            keyDates.firstOrNull { it.keyDateType.code == KeyDateTypes.POST_SENTENCE_SUPERVISION_END_DATE.code }
        val expectedPrisonOffenderManagerHandoverStartDate =
            keyDates.firstOrNull { it.keyDateType.code == KeyDateTypes.POM_HANDOVER_START_DATE.code }
        val expectedPrisonOffenderManagerHandoverDate =
            keyDates.firstOrNull { it.keyDateType.code == KeyDateTypes.RO_HANDOVER_DATE.code }

        return CustodyRelatedKeyDates(
            conditionalReleaseDate?.keyDate,
            licenceExpiryDate?.keyDate,
            hdcEligibilityDate?.keyDate,
            paroleEligibilityDate?.keyDate,
            sentenceExpiryDate?.keyDate,
            expectedReleaseDate?.keyDate,
            postSentenceSupervisionEndDate?.keyDate,
            expectedPrisonOffenderManagerHandoverStartDate?.keyDate,
            expectedPrisonOffenderManagerHandoverDate?.keyDate,
        )
    }

}

enum class KeyDateTypes(val code: String) {
    AUTOMATIC_CONDITIONAL_RELEASE_DATE("ACR"),
    EXPECTED_RELEASE_DATE("EXP"),
    HDC_EXPECTED_DATE("HDE"),
    PAROLE_ELIGIBILITY_DATE("PED"),
    POST_SENTENCE_SUPERVISION_END_DATE("PSSED"),
    POM_HANDOVER_START_DATE("POM1"),
    RO_HANDOVER_DATE("POM2"),
    LICENCE_EXPIRY_DATE("LED"),
    SENTENCE_EXPIRY_DATE("SED")
}



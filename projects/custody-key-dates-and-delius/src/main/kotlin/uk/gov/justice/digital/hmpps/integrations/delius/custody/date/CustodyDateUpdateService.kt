package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.client.RestClientUtils.nullIfNotFound
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.crds.CrdsApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateType.*
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateCalculator.electronicMonitoringEndDate
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateCalculator.finalThirdDate
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateCalculator.pssEndDateIfPss
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateCalculator.suspensionDateIfReset
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.findKeyDateType
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@Service
@Transactional
class CustodyDateUpdateService(
    private val prisonApi: PrisonApiClient,
    private val personRepository: PersonRepository,
    private val custodyRepository: CustodyRepository,
    private val disposalRepository: DisposalRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val keyDateRepository: KeyDateRepository,
    private val contactService: ContactService,
    private val telemetryService: TelemetryService,
    private val crdsApiClient: CrdsApiClient,
    private val featureFlags: FeatureFlags,
) {
    fun updateKeyDates(nomsId: String, dryRun: Boolean = false, clientSource: String = "messaging"): Boolean {
        val booking = nullIfNotFound { prisonApi.getBookingFromNomsNumber(nomsId.uppercase()) } ?: return false
        return updateKeyDates(booking, dryRun, clientSource)
    }

    fun updateKeyDates(bookingId: Long, dryRun: Boolean = false, clientSource: String = "messaging"): Boolean {
        val booking = prisonApi.getBooking(bookingId)
        return updateKeyDates(booking, dryRun, clientSource)
    }

    fun updateKeyDates(booking: Booking, dryRun: Boolean = false, clientSource: String = "messaging"): Boolean {
        val updated = getUpdatedCustodyDates(booking, clientSource)
        return saveKeyDates(updated, booking.offenderNo, booking.bookingNo, dryRun, clientSource)
    }

    private fun getUpdatedCustodyDates(
        booking: Booking,
        clientSource: String = "messaging"
    ): List<KeyDate> {
        val telemetry =
            mapOf("nomsNumber" to booking.offenderNo, "bookingRef" to booking.bookingNo, "clientSource" to clientSource)

        if (!booking.active) return noUpdate("BookingNotActive", telemetry)
        val sentenceDetail = prisonApi.getSentenceDetail(booking.id)
        val person = personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)
            ?: return noUpdate("MissingNomsNumber", telemetry)
        val custodyId = custodyRepository.findCustodyId(person.id, booking.bookingNo).run {
            if (size > 1) return noUpdate("DuplicateBookingRef", telemetry)
            singleOrNull() ?: return noUpdate("MissingBookingRef", telemetry)
        }

        return custodyRepository.findCustodyById(custodyRepository.findForUpdate(custodyId))
            .calculateKeyDateChanges(sentenceDetail)
    }

    private fun Custody.calculateKeyDateChanges(sentenceDetail: SentenceDetail) = listOfNotNull(
        keyDate(LICENCE_EXPIRY_DATE.code, sentenceDetail.licenceExpiryDate),
        keyDate(AUTOMATIC_CONDITIONAL_RELEASE_DATE.code, sentenceDetail.conditionalReleaseDate),
        keyDate(PAROLE_ELIGIBILITY_DATE.code, sentenceDetail.paroleEligibilityDate),
        keyDate(SENTENCE_EXPIRY_DATE.code, sentenceDetail.sentenceExpiryDate),
        keyDate(EXPECTED_RELEASE_DATE.code, sentenceDetail.confirmedReleaseDate),
        keyDate(HDC_EXPECTED_DATE.code, sentenceDetail.homeDetentionCurfewEligibilityDate),
        keyDate(POST_SENTENCE_SUPERVISION_END_DATE.code, sentenceDetail.pssEndDateIfPss(this)),
        keyDate(SUSPENSION_DATE_IF_RESET.code, sentenceDetail.suspensionDateIfReset(this))
    ) + if (!featureFlags.enabled("calculate-key-dates-from-delius") && disposal.type.determinateCustody) {
        val envelope = nullIfNotFound { crdsApiClient.getOperativeSentenceEnvelope(disposal.event.person.nomsId!!) }
        listOfNotNull(
            keyDate(ELECTRONIC_MONITORING_END_DATE.code, sentenceDetail.electronicMonitoringEndDate(envelope)),
            keyDate(FINAL_THIRD_START_DATE.code, sentenceDetail.finalThirdDate(envelope)),
        )
    } else emptyList()

    private fun getDerivedKeyDates(person: Person, updated: List<KeyDate>): List<KeyDate> =
        if (featureFlags.enabled("calculate-key-dates-from-delius")) {
            custodyRepository.findAllSentencesByPersonId(person.id).mapNotNull { it.custody }.flatMap { custody ->
                val keyDates = updated.filter { it.custody?.id == custody.id } + custody.keyDates
                listOfNotNull(
                    custody.keyDate(ELECTRONIC_MONITORING_END_DATE.code, custody.electronicMonitoringEndDate(keyDates)),
                    custody.keyDate(FINAL_THIRD_START_DATE.code, custody.finalThirdDate(keyDates))
                )
            }
        } else emptyList()

    private fun setSdsPlusFlag(person: Person) {
        val sentences = custodyRepository.findAllSentencesByPersonId(person.id).ifEmpty { null } ?: return
        val envelope = nullIfNotFound { crdsApiClient.getOperativeSentenceEnvelope(person.nomsId!!) }
        sentences.forEach {
            val previousSdsPlusValue = it.sdsPlus

            if (envelope != null) {
                it.sdsPlus = envelope.containsAnSDSPlusSentence
                disposalRepository.save(it)
            }

            // Also remove final third date from SDS+ sentences
            val removed = if (it.sdsPlus == true) {
                keyDateRepository.deleteByCustodyDisposalIdAndTypeCode(it.id, FINAL_THIRD_START_DATE.code)
            } else 0

            telemetryService.trackEvent(
                "SdsPlusFlagUpdated",
                mapOf(
                    "crn" to person.crn,
                    "nomsNumber" to person.nomsId,
                    "eventNumber" to it.event.eventNumber,
                    "sdsPlus" to it.sdsPlus.toString(),
                    "sdsPlusBefore" to previousSdsPlusValue.toString(),
                    "sdsPlusChanged" to (it.sdsPlus != previousSdsPlusValue).toString(),
                    "finalThirdRemoved" to removed.toString(),
                )
            )
        }
    }

    private fun saveKeyDates(
        updatedCustodyDates: List<KeyDate>,
        nomsId: String,
        bookingNo: String,
        dryRun: Boolean = false,
        clientSource: String = "messaging"
    ): Boolean {
        val telemetry = mapOf("nomsNumber" to nomsId, "bookingRef" to bookingNo, "clientSource" to clientSource)
        val person = personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(nomsId) ?: return false

        if (!dryRun) {
            // Set disposal.sdsPlus on all active custodial disposals
            setSdsPlusFlag(person)
        }

        // Derive key dates from Delius data for all active custodial disposals
        val changes = updatedCustodyDates + getDerivedKeyDates(person, updatedCustodyDates)

        if (changes.isEmpty()) {
            noUpdate("KeyDatesUnchanged", telemetry)
            return false
        } else {
            if (!dryRun) {
                keyDateRepository.saveAll(changes)
                // Create a contact for each updated sentence
                changes.groupBy { it.custody!! }.forEach { (custody, dates) ->
                    contactService.createForKeyDateChanges(custody, dates)
                }
            }
            telemetryService.trackEvent(
                if (dryRun) "KeyDatesDryRun" else "KeyDatesUpdated",
                telemetry + changes.associateBy({ it.type.code }, { it.date.toString() })
            )
            return !dryRun
        }
    }

    private fun Custody.keyDate(code: String, date: LocalDate?): KeyDate? = date?.let {
        val existing = keyDates.filter { it.type.code == code }.removeDuplicates()
        return if (existing != null) {
            existing.changeDate(date)
        } else {
            val kdt = referenceDataRepository.findKeyDateType(code)
            KeyDate(this, kdt, date)
        }
    }

    private fun List<KeyDate>.removeDuplicates(): KeyDate? {
        if (size > 1) {
            drop(1).forEach { keyDateRepository.delete(it) }
            keyDateRepository.flush() // Required to avoid unique key constraint, by ensuring delete happens before insert
        }
        return firstOrNull()
    }

    private fun noUpdate(message: String, telemetry: Map<String, String>): List<KeyDate> {
        telemetryService.trackEvent(message, telemetry)
        return emptyList()
    }
}

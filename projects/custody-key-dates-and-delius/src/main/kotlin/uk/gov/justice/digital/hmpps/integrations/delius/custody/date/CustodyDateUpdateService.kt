package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClientResponseException
import uk.gov.justice.digital.hmpps.client.RestClientUtils.nullIfNotFound
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.crds.CrdsApiClient
import uk.gov.justice.digital.hmpps.integrations.crds.OperativeSentenceEnvelope
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
    fun updateCustodyKeyDates(nomsId: String, dryRun: Boolean = false, clientSource: String = "messaging") = try {
        val booking = prisonApi.getBookingFromNomsNumber(nomsId.uppercase())
        updateCustodyKeyDates(booking, dryRun, clientSource)
    } catch (e: RestClientResponseException) {
        if (e.statusCode != HttpStatus.NOT_FOUND) throw e else false
    }

    fun updateCustodyKeyDates(bookingId: Long): Boolean {
        val booking = prisonApi.getBooking(bookingId)
        return updateCustodyKeyDates(booking)
    }

    private fun updateCustodyKeyDates(
        booking: Booking,
        dryRun: Boolean = false,
        clientSource: String = "messaging"
    ): Boolean {
        if (!booking.active) return noUpdate("BookingNotActive", booking.telemetry(clientSource))
        val calculateDatesFromDelius = featureFlags.enabled("calculate-key-dates-from-delius")
        val sentenceDetail = prisonApi.getSentenceDetail(booking.id)
        val person = personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)
            ?: return noUpdate("MissingNomsNumber", booking.telemetry(clientSource))
        val custodyId = custodyRepository.findCustodyId(person.id, booking.bookingNo).run {
            if (size > 1) return noUpdate("DuplicateBookingRef", booking.telemetry(clientSource))
            singleOrNull() ?: return noUpdate("MissingBookingRef", booking.telemetry(clientSource))
        }
        val custody = custodyRepository.findCustodyById(custodyRepository.findForUpdate(custodyId))
        // Only fetch CRDS data when feature flag is disabled
        val envelope = if (custody.disposal.type.determinateCustody) {
            nullIfNotFound { crdsApiClient.getOperativeSentenceEnvelope(booking.offenderNo) }
        } else null
        // Set disposal.sdsPlus on all active custodial disposals
        if (!dryRun) setSdsPlusFlag(envelope, person)

        val updated = calculateKeyDateChanges(sentenceDetail, custody, envelope, calculateDatesFromDelius)
        if (updated.isEmpty()) {
            return noUpdate("KeyDatesUnchanged", booking.telemetry(clientSource))
        } else {
            if (!dryRun) {
                keyDateRepository.saveAll(updated)
                contactService.createForKeyDateChanges(custody, updated)
            }
            telemetryService.trackEvent(
                if (dryRun) "KeyDatesDryRun" else "KeyDatesUpdated",
                booking.telemetry(clientSource) + updated.associateBy({ it.type.code }, { it.date.toString() })
            )
            return !dryRun
        }
    }

    private fun setSdsPlusFlag(
        envelope: OperativeSentenceEnvelope?,
        person: Person
    ) {
        custodyRepository.findAllSentencesByPersonId(person.id).forEach {
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

    private fun calculateKeyDateChanges(
        sentenceDetail: SentenceDetail,
        custody: Custody,
        envelope: OperativeSentenceEnvelope?,
        calculateDatesFromDelius: Boolean
    ) = listOfNotNull(
        custody.keyDate(LICENCE_EXPIRY_DATE.code, sentenceDetail.licenceExpiryDate),
        custody.keyDate(AUTOMATIC_CONDITIONAL_RELEASE_DATE.code, sentenceDetail.conditionalReleaseDate),
        custody.keyDate(PAROLE_ELIGIBILITY_DATE.code, sentenceDetail.paroleEligibilityDate),
        custody.keyDate(SENTENCE_EXPIRY_DATE.code, sentenceDetail.sentenceExpiryDate),
        custody.keyDate(EXPECTED_RELEASE_DATE.code, sentenceDetail.confirmedReleaseDate),
        custody.keyDate(HDC_EXPECTED_DATE.code, sentenceDetail.homeDetentionCurfewEligibilityDate),
        custody.keyDate(POST_SENTENCE_SUPERVISION_END_DATE.code, sentenceDetail.pssEndDateIfPss(custody)),
        custody.keyDate(SUSPENSION_DATE_IF_RESET.code, sentenceDetail.suspensionDateIfReset(custody))
    ) + if (calculateDatesFromDelius) listOfNotNull(
        custody.keyDate(ELECTRONIC_MONITORING_END_DATE.code, sentenceDetail.electronicMonitoringEndDate(custody)),
        custody.keyDate(FINAL_THIRD_START_DATE.code, sentenceDetail.finalThirdDate(custody))
    ) else listOfNotNull(
        custody.keyDate(ELECTRONIC_MONITORING_END_DATE.code, sentenceDetail.electronicMonitoringEndDate(envelope)),
        custody.keyDate(FINAL_THIRD_START_DATE.code, sentenceDetail.finalThirdDate(envelope)),
    )

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

    private fun Booking.telemetry(clientSource: String) = mapOf(
        "nomsNumber" to offenderNo,
        "bookingRef" to bookingNo,
        "clientSource" to clientSource
    )

    private fun noUpdate(message: String, telemetry: Map<String, String>): Boolean {
        telemetryService.trackEvent(message, telemetry)
        return false
    }
}

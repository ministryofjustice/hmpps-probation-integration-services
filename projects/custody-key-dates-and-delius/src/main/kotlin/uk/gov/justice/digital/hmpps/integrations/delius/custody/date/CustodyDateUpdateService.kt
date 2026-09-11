package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClientResponseException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.crds.CrdsApiClient
import uk.gov.justice.digital.hmpps.integrations.crds.OperativeSentenceEnvelope
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateType.*
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.findKeyDateType
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
    private val keyDateCalculator: KeyDateCalculator,
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
        val isStatutoryCustodyDeterminateSentence = custody.disposal?.isDisposalL1Sc() == true
        // Only fetch CRDS data when feature flag is disabled
        val envelope = if (!calculateDatesFromDelius && isStatutoryCustodyDeterminateSentence) {
            try {
                crdsApiClient.getOperativeSentenceEnvelope(booking.offenderNo)
            } catch (e: RestClientResponseException) {
                if (e.statusCode == HttpStatus.NOT_FOUND) null else throw e
            }
        } else null
        val updated = calculateKeyDateChanges(
            sentenceDetail,
            custody,
            envelope,
            calculateDatesFromDelius,
            isStatutoryCustodyDeterminateSentence
        )
        if (updated.isEmpty()) {
            return noUpdate("KeyDatesUnchanged", booking.telemetry(clientSource))
        } else {
            if (!dryRun) {
                keyDateRepository.saveAll(updated)
                contactService.createForKeyDateChanges(custody, updated)
                // Only update disposal.sdsPlus when using CRDS (feature flag off)
                if (!calculateDatesFromDelius && isStatutoryCustodyDeterminateSentence && envelope != null) {
                    if (booking.id != envelope.bookingId) {
                        telemetryService.trackEvent(
                            "SentenceEnvelopeBookingIdMismatch", mapOf(
                                "bookingId" to booking.id.toString(),
                                "envelopeBookingId" to envelope.bookingId.toString()
                            )
                        )
                    } else {
                        custody.disposal.let { disposal ->
                            disposal.sdsPlus = envelope.containsAnSDSPlusSentence
                            disposalRepository.save(disposal)
                        }
                    }
                }
            }
            telemetryService.trackEvent(
                if (dryRun) "KeyDatesDryRun" else "KeyDatesUpdated",
                booking.telemetry(clientSource) + updated.associateBy({ it.type.code }, { it.date.toString() })
            )
            return !dryRun
        }
    }

    private fun calculateKeyDateChanges(
        sentenceDetail: SentenceDetail,
        custody: Custody,
        envelope: OperativeSentenceEnvelope?,
        calculateDatesFromDelius: Boolean,
        isDisposalEligibleForFinalThirdDate: Boolean
    ) =
        if (calculateDatesFromDelius) {
            calculateKeyDateChangesFromDelius(sentenceDetail, custody, isDisposalEligibleForFinalThirdDate)
        } else calculateKeyDateChangesUsingCRDS(sentenceDetail, custody, envelope)

    private fun calculateKeyDateChangesUsingCRDS(
        sentenceDetail: SentenceDetail,
        custody: Custody,
        envelope: OperativeSentenceEnvelope?
    ) =
        listOfNotNull(
            custody.keyDate(LICENCE_EXPIRY_DATE.code, sentenceDetail.licenceExpiryDate),
            custody.keyDate(AUTOMATIC_CONDITIONAL_RELEASE_DATE.code, sentenceDetail.conditionalReleaseDate),
            custody.keyDate(PAROLE_ELIGIBILITY_DATE.code, sentenceDetail.paroleEligibilityDate),
            custody.keyDate(SENTENCE_EXPIRY_DATE.code, sentenceDetail.sentenceExpiryDate),
            custody.keyDate(EXPECTED_RELEASE_DATE.code, sentenceDetail.confirmedReleaseDate),
            custody.keyDate(HDC_EXPECTED_DATE.code, sentenceDetail.homeDetentionCurfewEligibilityDate),
            custody.keyDate(
                POST_SENTENCE_SUPERVISION_END_DATE.code,
                sentenceDetail.postSentenceSupervisionEndDate.takeIf { custody.disposal?.type?.pssRequirement == true }),
            custody.keyDate(
                SUSPENSION_DATE_IF_RESET.code,
                keyDateCalculator.suspensionDateIfReset(sentenceDetail, custody)
            ),
            envelope?.let { envelope ->
                custody.keyDate(
                    PRESUMPTIVE_EM_END_DATE.code,
                    keyDateCalculator.presumptiveElectronicMonitoringEndDate(sentenceDetail, envelope)
                )
            },
            envelope?.let { envelope ->
                custody.keyDate(FINAL_THIRD_START_DATE.code, keyDateCalculator.finalThirdDate(sentenceDetail, envelope))
            }
        )

    private fun calculateKeyDateChangesFromDelius(
        sentenceDetail: SentenceDetail,
        custody: Custody,
        isDisposalEligibleForFinalThirdDate: Boolean,
    ): List<KeyDate> {
        val sentenceEndDate = custody.deliusSentenceEndDate(sentenceDetail)

        return listOfNotNull(
            custody.keyDate(LICENCE_EXPIRY_DATE.code, sentenceDetail.licenceExpiryDate),
            custody.keyDate(AUTOMATIC_CONDITIONAL_RELEASE_DATE.code, sentenceDetail.conditionalReleaseDate),
            custody.keyDate(PAROLE_ELIGIBILITY_DATE.code, sentenceDetail.paroleEligibilityDate),
            custody.keyDate(SENTENCE_EXPIRY_DATE.code, sentenceDetail.sentenceExpiryDate),
            custody.keyDate(EXPECTED_RELEASE_DATE.code, sentenceDetail.confirmedReleaseDate),
            custody.keyDate(HDC_EXPECTED_DATE.code, sentenceDetail.homeDetentionCurfewEligibilityDate),
            custody.keyDate(
                POST_SENTENCE_SUPERVISION_END_DATE.code,
                sentenceDetail.postSentenceSupervisionEndDate.takeIf { custody.disposal?.type?.pssRequirement == true }),
            custody.keyDate(
                SUSPENSION_DATE_IF_RESET.code,
                keyDateCalculator.suspensionDateIfReset(sentenceDetail, custody)
            ),
            custody.keyDate(
                PRESUMPTIVE_EM_END_DATE.code,
                sentenceEndDate?.takeIf { isDisposalEligibleForFinalThirdDate }?.let {
                    keyDateCalculator.presumptiveElectronicMonitoringEndDateFromDelius(
                        it,
                        custody.disposal?.date,
                        custody.disposal?.sdsPlus
                    )
                }
            ),
            custody.keyDate(
                FINAL_THIRD_START_DATE.code,
                sentenceEndDate?.takeIf {
                    isDisposalEligibleForFinalThirdDate &&
                        custody.disposal?.sdsPlus != true &&
                        custody.disposal?.date?.isAfter(it) == false
                }
                    ?.let {
                        keyDateCalculator.finalThirdDateFromDelius(it, custody.disposal?.date)
                    }
            )
        )
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

    private fun Custody.deliusSentenceEndDate(sentenceDetail: SentenceDetail) =
        sentenceDetail.sentenceExpiryDate ?: disposal?.notionalEndDate

    private fun Disposal.isDisposalL1Sc(): Boolean = type.isStatutoryCustody && type.determinateSentence

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

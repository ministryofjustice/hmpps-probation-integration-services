package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClientResponseException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
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
import java.time.temporal.ChronoUnit.DAYS

@Service
@Transactional
class CustodyDateUpdateService(
    private val prisonApi: PrisonApiClient,
    private val personRepository: PersonRepository,
    private val custodyRepository: CustodyRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val keyDateRepository: KeyDateRepository,
    private val contactService: ContactService,
    private val telemetryService: TelemetryService,
    private val featureFlags: FeatureFlags,
) {
    fun updateCustodyKeyDates(nomsId: String, dryRun: Boolean = false, clientSource: String = "messaging") {
        try {
            val booking = prisonApi.getBookingFromNomsNumber(nomsId.uppercase())
            updateCustodyKeyDates(booking, dryRun, clientSource)
        } catch (e: RestClientResponseException) {
            if (e.statusCode != HttpStatus.NOT_FOUND) throw e
        }
    }

    fun updateCustodyKeyDates(bookingId: Long) {
        val booking = prisonApi.getBooking(bookingId)
        updateCustodyKeyDates(booking)
    }

    private fun updateCustodyKeyDates(booking: Booking, dryRun: Boolean = false, clientSource: String = "messaging") {
        if (!booking.active) return telemetryService.trackEvent("BookingNotActive", booking.telemetry(clientSource))
        val sentenceDetail = prisonApi.getSentenceDetail(booking.id)
        val person = personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)
            ?: return telemetryService.trackEvent("MissingNomsNumber", booking.telemetry(clientSource))
        val custodyId = custodyRepository.findCustodyId(person.id, booking.bookingNo).run {
            if (size > 1) return telemetryService.trackEvent("DuplicateBookingRef", booking.telemetry(clientSource))
            singleOrNull() ?: return telemetryService.trackEvent("MissingBookingRef", booking.telemetry(clientSource))
        }
        val custody = custodyRepository.findCustodyById(custodyRepository.findForUpdate(custodyId))
        val updated = calculateKeyDateChanges(sentenceDetail, custody)
        if (updated.isEmpty()) {
            telemetryService.trackEvent("KeyDatesUnchanged", booking.telemetry(clientSource))
        } else {
            if (!dryRun) {
                keyDateRepository.saveAll(updated)
                contactService.createForKeyDateChanges(custody, updated)
            }
            telemetryService.trackEvent(
                if (dryRun) "KeyDatesDryRun" else "KeyDatesUpdated",
                booking.telemetry(clientSource) + updated.associateBy({ it.type.code }, { it.date.toString() })
            )
        }
    }

    private fun calculateKeyDateChanges(sentenceDetail: SentenceDetail, custody: Custody) = listOfNotNull(
        custody.keyDate(LICENCE_EXPIRY_DATE.code, sentenceDetail.licenceExpiryDate),
        custody.keyDate(AUTOMATIC_CONDITIONAL_RELEASE_DATE.code, sentenceDetail.conditionalReleaseDate),
        custody.keyDate(PAROLE_ELIGIBILITY_DATE.code, sentenceDetail.paroleEligibilityDate),
        custody.keyDate(SENTENCE_EXPIRY_DATE.code, sentenceDetail.sentenceExpiryDate),
        custody.keyDate(EXPECTED_RELEASE_DATE.code, sentenceDetail.confirmedReleaseDate),
        custody.keyDate(HDC_EXPECTED_DATE.code, sentenceDetail.homeDetentionCurfewEligibilityDate),
        custody.keyDate(POST_SENTENCE_SUPERVISION_END_DATE.code, sentenceDetail.postSentenceSupervisionEndDate),
        custody.keyDate(SUSPENSION_DATE_IF_RESET.code, suspensionDateIfReset(sentenceDetail, custody)),
    )

    fun suspensionDateIfReset(sentenceDetail: SentenceDetail, custody: Custody): LocalDate? = custody.disposal
        ?.takeIf { featureFlags.enabled("suspension-date-if-reset") }
        ?.takeIf { it.type.determinateSentence }
        ?.let {
            val startDate = it.event.firstReleaseDate ?: sentenceDetail.conditionalReleaseDate ?: return null
            val endDate = sentenceDetail.sentenceExpiryDate ?: return null
            return if (startDate < endDate) startDate.plusDays(DAYS.between(startDate, endDate) * 2 / 3) else null
        }

    private fun Custody.keyDate(code: String, date: LocalDate?): KeyDate? = date?.let {
        val existing = keyDates.find(code)
        return if (existing != null) {
            existing.changeDate(date)
        } else {
            val kdt = referenceDataRepository.findKeyDateType(code)
            KeyDate(this, kdt, date)
        }
    }

    private fun List<KeyDate>.find(code: String): KeyDate? = firstOrNull { it.type.code == code }

    private fun Booking.telemetry(clientSource: String) = mapOf(
        "nomsNumber" to offenderNo,
        "bookingRef" to bookingNo,
        "clientSource" to clientSource
    )
}

package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClientResponseException
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.findKeyDateType
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
@Transactional
class CustodyDateUpdateService(
    private val prisonApi: PrisonApiClient,
    private val personRepository: PersonRepository,
    private val custodyRepository: CustodyRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val keyDateRepository: KeyDateRepository,
    private val contactService: ContactService,
    private val telemetryService: TelemetryService
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
            telemetryService.trackEvent(
                "KeyDatesUnchanged",
                booking.telemetry(clientSource)
            )
        } else {
            if (!dryRun) {
                updated.ifNotEmpty(keyDateRepository::saveAll)
                contactService.createForKeyDateChanges(custody, updated)
            }
            telemetryService.trackEvent(
                if (dryRun) "KeyDatesDryRun" else "KeyDatesUpdated",
                booking.telemetry(clientSource) +
                    updated.associateBy({ it.type.code }, { it.date.toString() })
            )
        }
    }

    private fun List<KeyDate>.ifNotEmpty(code: (List<KeyDate>) -> Unit) = code(this)

    private fun calculateKeyDateChanges(
        sentenceDetail: SentenceDetail,
        custody: Custody
    ): List<KeyDate> = CustodyDateType.entries.mapNotNull { cdt ->
        cdt.field.getter.call(sentenceDetail)?.let {
            val existing = custody.keyDates.find(cdt.code)
            if (existing != null) {
                existing.changeDate(it)
            } else {
                val kdt = referenceDataRepository.findKeyDateType(cdt.code)
                KeyDate(null, custody, kdt, it)
            }
        }
    }

    private fun List<KeyDate>.find(code: String): KeyDate? = firstOrNull { it.type.code == code }

    private fun Booking.telemetry(clientSource: String) = mapOf(
        "nomsNumber" to offenderNo,
        "bookingRef" to bookingNo,
        "clientSource" to clientSource
    )
}

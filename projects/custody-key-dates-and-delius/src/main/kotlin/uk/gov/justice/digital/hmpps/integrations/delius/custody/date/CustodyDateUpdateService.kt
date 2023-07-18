package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    fun updateCustodyKeyDates(nomsId: String) {
        val booking = prisonApi.getBookingFromNomsNumber(nomsId)
        updateCustodyKeyDates(booking)
    }

    fun updateCustodyKeyDates(bookingId: Long) {
        val booking = prisonApi.getBooking(bookingId)
        updateCustodyKeyDates(booking)
    }

    private fun updateCustodyKeyDates(booking: Booking) {
        if (!booking.active) return telemetryService.trackEvent("BookingNotActive", booking.telemetry())
        val sentenceDetail = prisonApi.getSentenceDetail(booking.id)
        val person = personRepository.findByNomsIdAndSoftDeletedIsFalse(booking.offenderNo)
            ?: return telemetryService.trackEvent("MissingNomsNumber", booking.telemetry())
        val custody = custodyRepository.findCustody(person.id, booking.bookingNo).run {
            if (size > 1) return telemetryService.trackEvent("DuplicateBookingRef", booking.telemetry())
            singleOrNull() ?: return telemetryService.trackEvent("MissingBookingRef", booking.telemetry())
        }
        val (deleted, updated) = calculateKeyDateChanges(sentenceDetail, custody).partition { it.softDeleted }
        keyDateRepository.deleteAll(deleted)
        keyDateRepository.saveAll(updated)
        contactService.createForKeyDateChanges(custody, updated, deleted)
        telemetryService.trackEvent(
            "KeyDatesUpdated",
            booking.telemetry() +
                updated.associateBy({ it.type.code }, { it.date.toString() }) +
                deleted.associateBy({ it.type.code }, { "deleted" })
        )
    }

    private fun calculateKeyDateChanges(
        sentenceDetail: SentenceDetail,
        custody: Custody
    ): List<KeyDate> = CustodyDateType.entries.mapNotNull { cdt ->
        val date = cdt.field.getter.call(sentenceDetail)
        if (date != null) {
            val existing = custody.keyDates.find(cdt.code)
            if (existing != null) {
                existing.date = date
                existing
            } else {
                val kdt = referenceDataRepository.findKeyDateType(cdt.code)
                KeyDate(null, custody, kdt, date)
            }
        } else {
            custody.keyDates.find(cdt.code)?.let { it.softDeleted = true; it }
        }
    }

    private fun List<KeyDate>.find(code: String): KeyDate? = firstOrNull { it.type.code == code }

    private fun Booking.telemetry() = mapOf("nomsNumber" to offenderNo, "bookingRef" to bookingNo)
}

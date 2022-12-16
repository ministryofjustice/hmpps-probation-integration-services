package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.findKeyDateType
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail

@Service
class CustodyDateUpdateService(
    private val prisonApi: PrisonApiClient,
    private val personRepository: PersonRepository,
    private val custodyRepository: CustodyRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val keyDateRepository: KeyDateRepository,
    private val contactService: ContactService
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
        if (booking.active) {
            val sentenceDetail = prisonApi.getSentenceDetail(booking.id)
            val person = personRepository.findByNomsIdAndSoftDeletedIsFalse(booking.offenderNo) ?: return
            val custody = findCustody(person.id, booking.bookingNo)
            val changes = calculateKeyDateChanges(sentenceDetail, custody).groupBy { it.softDeleted }
            changes[true]?.let {
                if (it.isNotEmpty()) keyDateRepository.deleteAll(it)
            }
            changes[false]?.let {
                if (it.isNotEmpty()) keyDateRepository.saveAll(it)
            }
            contactService.createForKeyDateChanges(custody, changes[false] ?: listOf(), changes[true] ?: listOf())
        }
    }

    private fun calculateKeyDateChanges(
        sentenceDetail: SentenceDetail,
        custody: Custody
    ): List<KeyDate> = CustodyDateType.values().mapNotNull { cdt ->
        val date = cdt.field.getter.call(sentenceDetail)
        if (date != null) {
            val existing = custody.keyDates.find(cdt.code)
            if (existing != null) {
                existing.date = date
                return@mapNotNull existing
            } else {
                val kdt = referenceDataRepository.findKeyDateType(cdt.code)
                return@mapNotNull KeyDate(null, custody, kdt, date)
            }
        } else {
            return@mapNotNull custody.keyDates.find(cdt.code)?.let { it.softDeleted = true; it }
        }
    }

    private fun findCustody(
        personId: Long,
        bookingRef: String
    ): Custody {
        val sentences = custodyRepository.findCustody(personId, bookingRef)
        if (sentences.isEmpty()) throw NotFoundException("Custody", "bookingRef", bookingRef)
        if (sentences.size > 1) throw ConflictException("Multiple custody records matched booking reference $bookingRef")
        return sentences[0]
    }

    private fun List<KeyDate>.find(code: String): KeyDate? = firstOrNull { it.type.code == code }
}

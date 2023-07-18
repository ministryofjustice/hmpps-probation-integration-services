package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class CustodyDateUpdateServiceTest {

    @Mock
    lateinit var prisonApi: PrisonApiClient

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var custodyRepository: CustodyRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var keyDateRepository: KeyDateRepository

    @Mock
    lateinit var contactService: ContactService

    @Mock
    lateinit var telemetryService: TelemetryService

    @InjectMocks
    lateinit var custodyDateUpdateService: CustodyDateUpdateService

    @Test
    fun `inactive bookings are not processed`() {
        val inactive = Booking(37, "FA37K", false, "AY356Y")

        whenever(prisonApi.getBooking(inactive.id, basicInfo = false, extraInfo = true)).thenReturn(inactive)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = inactive.id)

        verify(personRepository, never()).findByNomsIdAndSoftDeletedIsFalse(any())
        verify(contactService, never()).createForKeyDateChanges(any(), any(), any())
        verify(telemetryService).trackEvent(eq("BookingNotActive"), any(), any())
    }

    @Test
    fun `messages for people without a noms number are ignored`() {
        val booking = Booking(127, "FG37K", true, "AB356Z")

        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail(conditionalReleaseDate = LocalDate.now()))
        whenever(personRepository.findByNomsIdAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(null)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(contactService, never()).createForKeyDateChanges(any(), any(), any())
        verify(telemetryService).trackEvent(eq("MissingNomsNumber"), any(), any())
    }

    @Test
    fun `Multiple matching custody records throws exception`() {
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId)

        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail())
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdAndSoftDeletedIsFalse(booking.offenderNo))
            .thenReturn(PersonGenerator.DEFAULT)
        whenever(custodyRepository.findCustody(PersonGenerator.DEFAULT.id, booking.bookingNo))
            .thenReturn(
                listOf(
                    SentenceGenerator.generateCustodialSentence(
                        disposal = SentenceGenerator.generateDisposal(SentenceGenerator.generateEvent()),
                        bookingRef = booking.bookingNo
                    ),
                    SentenceGenerator.generateCustodialSentence(
                        disposal = SentenceGenerator.generateDisposal(SentenceGenerator.generateEvent()),
                        bookingRef = booking.bookingNo
                    )
                )
            )

        assertThrows<ConflictException> { custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id) }

        verify(keyDateRepository, never()).saveAll(anyList())
        verify(keyDateRepository, never()).deleteAll(any())
        verify(contactService, never()).createForKeyDateChanges(any(), any(), any())
    }

    @Test
    fun `No matching custody records throws exception`() {
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId)

        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail())
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdAndSoftDeletedIsFalse(booking.offenderNo))
            .thenReturn(PersonGenerator.DEFAULT)
        whenever(custodyRepository.findCustody(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(listOf())

        val ex = assertThrows<NotFoundException> {
            custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)
        }

        assertThat(ex.message, equalTo("Custody with bookingRef of ${booking.bookingNo} not found"))
        verify(keyDateRepository, never()).saveAll(anyList())
        verify(keyDateRepository, never()).deleteAll(any())
        verify(contactService, never()).createForKeyDateChanges(any(), any(), any())
    }

    @Test
    fun `key date save and delete not called without appropriate key dates`() {
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId)

        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail())
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdAndSoftDeletedIsFalse(booking.offenderNo))
            .thenReturn(PersonGenerator.DEFAULT)
        whenever(custodyRepository.findCustody(PersonGenerator.DEFAULT.id, booking.bookingNo))
            .thenReturn(
                listOf(
                    SentenceGenerator.generateCustodialSentence(
                        disposal = SentenceGenerator.generateDisposal(SentenceGenerator.generateEvent()),
                        bookingRef = booking.bookingNo
                    )
                )
            )

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(keyDateRepository).saveAll(emptyList())
        verify(keyDateRepository).deleteAll(emptyList())
    }
}

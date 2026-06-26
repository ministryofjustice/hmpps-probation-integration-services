package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateCustodialSentence
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateDisposalType
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateEvent
import uk.gov.justice.digital.hmpps.integrations.crds.CrdsApiClient
import uk.gov.justice.digital.hmpps.integrations.crds.OperativeSentenceEnvelope
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.DatasetCode
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
    lateinit var crdsApiClient: CrdsApiClient

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

    lateinit var custodyDateUpdateService: CustodyDateUpdateService

    @BeforeEach
    fun setup() {
        custodyDateUpdateService = CustodyDateUpdateService(
            prisonApi,
            personRepository,
            custodyRepository,
            referenceDataRepository,
            keyDateRepository,
            contactService,
            telemetryService,
            crdsApiClient,
            KeyDateCalculator()
        )
    }

    @Test
    fun `inactive bookings are not processed`() {
        val inactive = Booking(37, "FA37K", false, "AY356Y")

        whenever(prisonApi.getBooking(inactive.id, basicInfo = false, extraInfo = true)).thenReturn(inactive)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = inactive.id)

        verify(personRepository, never()).findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(any())
        verify(contactService, never()).createForKeyDateChanges(any(), any())
        verify(telemetryService).trackEvent(eq("BookingNotActive"), any(), any())
    }

    @Test
    fun `messages for people without a noms number are ignored`() {
        val booking = Booking(127, "FG37K", true, "AB356Z")

        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail(conditionalReleaseDate = LocalDate.now()))
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(null)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(contactService, never()).createForKeyDateChanges(any(), any())
        verify(telemetryService).trackEvent(eq("MissingNomsNumber"), any(), any())
    }

    @Test
    fun `Multiple matching custody logged to telemetry`() {
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)

        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail())
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo))
            .thenReturn(PersonGenerator.DEFAULT)
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo))
            .thenReturn(listOf(42342562452L, 34345249134L))

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(keyDateRepository, never()).saveAll(anyList())
        verify(keyDateRepository, never()).deleteAll(any())
        verify(contactService, never()).createForKeyDateChanges(any(), any())
        verify(telemetryService).trackEvent(eq("DuplicateBookingRef"), any(), any())
    }

    @Test
    fun `No matching custody logged to telemetry`() {
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)

        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail())
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo))
            .thenReturn(PersonGenerator.DEFAULT)
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(listOf())

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(keyDateRepository, never()).saveAll(anyList())
        verify(keyDateRepository, never()).deleteAll(any())
        verify(contactService, never()).createForKeyDateChanges(any(), any())
        verify(telemetryService).trackEvent(eq("MissingBookingRef"), any(), any())
    }

    @Test
    fun `key date save and delete not called without appropriate key dates`() {
        setupCrdsMock()
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val custody = SentenceGenerator.generateCustodialSentence(
            disposal = SentenceGenerator.generateDisposal(SentenceGenerator.generateEvent()),
            bookingRef = booking.bookingNo
        )

        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail())
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo))
            .thenReturn(PersonGenerator.DEFAULT)
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo))
            .thenReturn(listOf(custody.id))
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(keyDateRepository, never()).saveAll(anyList())
        verify(keyDateRepository, never()).deleteAll(anyList())
    }

    @Test
    fun `PSSED is included when disposal type has PSS_RQMNT Y`() {
        setupCrdsMock()
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val pssDate = LocalDate.of(2025, 6, 1)
        val custody = generateCustodialSentence(
            disposal = generateDisposal(generateEvent(), generateDisposalType(pssRequirement = true)),
            bookingRef = booking.bookingNo
        )
        val pssedRef = ReferenceDataGenerator.KEY_DATE_TYPES[CustodyDateType.POST_SENTENCE_SUPERVISION_END_DATE.code]!!

        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail(postSentenceSupervisionEndDate = pssDate))
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo))
            .thenReturn(PersonGenerator.DEFAULT)
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo))
            .thenReturn(listOf(custody.id))
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                DatasetCode.KEY_DATE_TYPE,
                CustodyDateType.POST_SENTENCE_SUPERVISION_END_DATE.code
            )
        )
            .thenReturn(pssedRef)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(keyDateRepository).saveAll(
            check<List<KeyDate>> { saved ->
                assertThat(
                    saved.any { it.type.code == CustodyDateType.POST_SENTENCE_SUPERVISION_END_DATE.code },
                    equalTo(true)
                )
            }
        )
    }

    @Test
    fun `PSSED is excluded when disposal type does not have PSS_RQMNT Y`() {
        setupCrdsMock()
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val pssDate = LocalDate.of(2025, 6, 1)
        val custody = generateCustodialSentence(
            disposal = generateDisposal(generateEvent(), generateDisposalType(pssRequirement = null)),
            bookingRef = booking.bookingNo
        )

        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail(postSentenceSupervisionEndDate = pssDate))
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo))
            .thenReturn(PersonGenerator.DEFAULT)
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo))
            .thenReturn(listOf(custody.id))
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(keyDateRepository, never()).saveAll(anyList())
        verify(telemetryService).trackEvent(eq("KeyDatesUnchanged"), any(), any())
    }

    @Test
    fun `two-thirds point uses event first release date if present`() {
        val event = generateEvent(firstReleaseDate = LocalDate.of(2025, 1, 1))
        val disposal = generateDisposal(event)
        val custody = generateCustodialSentence(disposal = disposal, bookingRef = "ABC")

        val suspensionDateIfReset = KeyDateCalculator().suspensionDateIfReset(
            SentenceDetail(
                conditionalReleaseDate = LocalDate.of(2026, 1, 1),
                sentenceExpiryDate = LocalDate.of(2026, 1, 1)
            ), custody
        )

        assertThat(suspensionDateIfReset, equalTo(LocalDate.of(2025, 9, 1)))
    }

    @Test
    fun `two-thirds point is null when event is not determinate`() {
        val event = generateEvent()
        val disposal = generateDisposal(event, generateDisposalType("L2"))
        val custody = generateCustodialSentence(disposal = disposal, bookingRef = "ABC")

        val suspensionDateIfReset = KeyDateCalculator().suspensionDateIfReset(
            SentenceDetail(
                conditionalReleaseDate = LocalDate.of(2024, 1, 1),
                sentenceExpiryDate = LocalDate.of(2025, 1, 1)
            ), custody
        )

        assertThat(suspensionDateIfReset, nullValue())
    }

    private fun setupCrdsMock() {
        whenever(crdsApiClient.getOperativeSentenceEnvelope(any())).thenReturn(
            OperativeSentenceEnvelope(
                sentenceEnvelopeLengthInDays = 50L,
                containsAnSDSPlusSentence = true,
            )
        )
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `check two-thirds point`(
        conditionalReleaseDate: LocalDate?,
        sentenceExpiryDate: LocalDate?,
        expected: LocalDate?
    ) {
        val custody = generateCustodialSentence(disposal = generateDisposal(generateEvent()), bookingRef = "ABC")
        val suspensionDateIfReset = KeyDateCalculator().suspensionDateIfReset(
            SentenceDetail(
                conditionalReleaseDate = conditionalReleaseDate,
                sentenceExpiryDate = sentenceExpiryDate
            ), custody
        )

        assertThat(suspensionDateIfReset, equalTo(expected))
    }

    companion object {
        @JvmStatic
        private fun testCases() = listOf(
            arguments(null, LocalDate.of(2025, 1, 1), null),
            arguments(LocalDate.of(2025, 1, 1), null, null),
            arguments(LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 1), null),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1), null),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 3), LocalDate.of(2025, 1, 2)),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 4), LocalDate.of(2025, 1, 3)),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1), LocalDate.of(2025, 9, 1)),
            arguments(LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 30), LocalDate.of(2028, 3, 20)),
            arguments(LocalDate.of(2099, 6, 30), LocalDate.of(2120, 2, 29), LocalDate.of(2113, 4, 10)),
        )
    }
}

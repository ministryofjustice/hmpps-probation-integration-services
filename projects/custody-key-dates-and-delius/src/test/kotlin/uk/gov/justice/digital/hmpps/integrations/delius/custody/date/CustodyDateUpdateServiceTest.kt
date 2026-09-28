package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateCustodialSentence
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateDisposalType
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateEvent
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.crds.CrdsApiClient
import uk.gov.justice.digital.hmpps.integrations.crds.OperativeSentenceEnvelope
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateCalculator.suspensionDateIfReset
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
    lateinit var disposalRepository: DisposalRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var keyDateRepository: KeyDateRepository

    @Mock
    lateinit var contactService: ContactService

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var featureFlags: FeatureFlags

    lateinit var custodyDateUpdateService: CustodyDateUpdateService

    @BeforeEach
    fun setup() {
        custodyDateUpdateService = CustodyDateUpdateService(
            prisonApi = prisonApi,
            personRepository = personRepository,
            custodyRepository = custodyRepository,
            disposalRepository = disposalRepository,
            referenceDataRepository = referenceDataRepository,
            keyDateRepository = keyDateRepository,
            contactService = contactService,
            telemetryService = telemetryService,
            crdsApiClient = crdsApiClient,
            featureFlags = featureFlags,
        )
    }

    private fun featureFlagEnabled(enabled: Boolean) {
        whenever(featureFlags.enabled("calculate-key-dates-from-delius")).thenReturn(enabled)
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
        featureFlagEnabled(false)
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
        featureFlagEnabled(false)
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
        featureFlagEnabled(false)
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
        featureFlagEnabled(false)
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val custody = generateCustodialSentence(
            disposal = generateDisposal(generateEvent()),
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
        featureFlagEnabled(false)
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
        featureFlagEnabled(false)
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

        val suspensionDateIfReset = SentenceDetail(
            conditionalReleaseDate = LocalDate.of(2026, 1, 1),
            sentenceExpiryDate = LocalDate.of(2026, 1, 1)
        ).suspensionDateIfReset(custody)

        assertThat(suspensionDateIfReset, equalTo(LocalDate.of(2025, 9, 1)))
    }

    @Test
    fun `two-thirds point is null when event is not determinate`() {
        val event = generateEvent()
        val disposal = generateDisposal(event, generateDisposalType("L2"))
        val custody = generateCustodialSentence(disposal = disposal, bookingRef = "ABC")

        val suspensionDateIfReset = SentenceDetail(
            conditionalReleaseDate = LocalDate.of(2024, 1, 1),
            sentenceExpiryDate = LocalDate.of(2025, 1, 1)
        ).suspensionDateIfReset(custody)

        assertThat(suspensionDateIfReset, nullValue())
    }

    @ParameterizedTest
    @CsvSource("false, 2024-01-04", "true, 2024-01-09")
    fun `eligible SDS disposal updates flag and key dates according to SDS+ status`(
        sdsPlus: Boolean,
        expectedEmed: LocalDate
    ) {
        featureFlagEnabled(false)
        listOf(
            CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE,
            CustodyDateType.SENTENCE_EXPIRY_DATE,
            CustodyDateType.SUSPENSION_DATE_IF_RESET,
            CustodyDateType.ELECTRONIC_MONITORING_END_DATE,
            CustodyDateType.FINAL_THIRD_START_DATE
        ).filterNot { sdsPlus && it == CustodyDateType.FINAL_THIRD_START_DATE }.forEach { type ->
            whenever(
                referenceDataRepository.findByDatasetAndCode(
                    DatasetCode.KEY_DATE_TYPE,
                    type.code
                )
            ).thenReturn(ReferenceDataGenerator.KEY_DATE_TYPES[type.code]!!)
        }
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val disposal = generateDisposal(generateEvent())
        val custody = generateCustodialSentence(disposal = disposal, bookingRef = booking.bookingNo)
        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(
            SentenceDetail(
                conditionalReleaseDate = LocalDate.of(2024, 1, 1),
                sentenceExpiryDate = LocalDate.of(2025, 1, 1)
            )
        )
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(
            PersonGenerator.DEFAULT
        )
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(
            listOf(
                custody.id
            )
        )
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)

        whenever(crdsApiClient.getOperativeSentenceEnvelope(booking.offenderNo)).thenReturn(
            OperativeSentenceEnvelope(
                sentenceEnvelopeLengthInDays = 50L,
                containsAnSDSPlusSentence = sdsPlus,
                bookingId = booking.id
            )
        )
        whenever(custodyRepository.findAllSentencesByPersonId(PersonGenerator.DEFAULT.id)).thenReturn(listOf(disposal))
        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)
        assertThat(custody.disposal.sdsPlus, equalTo(sdsPlus))
        verify(disposalRepository).save(
            check<Disposal> {
                assertThat(it.sdsPlus, equalTo(sdsPlus))
            })
        verify(keyDateRepository).saveAll(
            check<List<KeyDate>> { saved ->
                assertThat(
                    saved.single { it.type.code == CustodyDateType.ELECTRONIC_MONITORING_END_DATE.code }.date,
                    equalTo(expectedEmed)
                )

                assertThat(
                    saved.filter { it.type.code == CustodyDateType.FINAL_THIRD_START_DATE.code }.map { it.date },
                    equalTo(if (sdsPlus) emptyList() else listOf(LocalDate.of(2024, 12, 15)))
                )
            })
        verify(keyDateRepository, times(if (sdsPlus) 1 else 0)).deleteByCustodyDisposalIdAndTypeCode(
            disposal.id, CustodyDateType.FINAL_THIRD_START_DATE.code
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `SDS+ dates and flag are not set when disposal sentence type is not SC`(calculateDatesFromDelius: Boolean) {
        featureFlagEnabled(calculateDatesFromDelius)
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val custody = generateCustodialSentence(
            disposal = generateDisposal(
                generateEvent(), generateDisposalType(sentenceType = "NC"), lengthInDays = 366L
            ),
            bookingRef = booking.bookingNo
        )
        listOf(
            CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE,
            CustodyDateType.SENTENCE_EXPIRY_DATE,
            CustodyDateType.SUSPENSION_DATE_IF_RESET,
        ).forEach { type ->
            whenever(
                referenceDataRepository.findByDatasetAndCode(
                    DatasetCode.KEY_DATE_TYPE,
                    type.code
                )
            ).thenReturn(ReferenceDataGenerator.KEY_DATE_TYPES[type.code]!!)
        }
        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(
            SentenceDetail(
                conditionalReleaseDate = LocalDate.of(2024, 1, 1),
                sentenceExpiryDate = LocalDate.of(2025, 1, 1)
            )
        )
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(
            PersonGenerator.DEFAULT
        )
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(
            listOf(custody.id)
        )
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(crdsApiClient, never()).getOperativeSentenceEnvelope(any())
        verify(disposalRepository, never()).save(any<Disposal>())
        verify(keyDateRepository).saveAll(
            check<List<KeyDate>> { saved ->
                assertThat(
                    saved.any { it.type.code == CustodyDateType.ELECTRONIC_MONITORING_END_DATE.code },
                    equalTo(false)
                )
                assertThat(saved.any { it.type.code == CustodyDateType.FINAL_THIRD_START_DATE.code }, equalTo(false))
            })
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `SDS+ dates and flag are not set when disposal is not L1`(calculateDatesFromDelius: Boolean) {
        featureFlagEnabled(calculateDatesFromDelius)
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val custody = generateCustodialSentence(
            disposal = generateDisposal(
                generateEvent(), generateDisposalType(requiredInformation = "L2"), lengthInDays = 366L
            ),
            bookingRef = booking.bookingNo
        )
        listOf(
            CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE,
            CustodyDateType.SENTENCE_EXPIRY_DATE,
        ).forEach { type ->
            whenever(
                referenceDataRepository.findByDatasetAndCode(
                    DatasetCode.KEY_DATE_TYPE,
                    type.code
                )
            ).thenReturn(ReferenceDataGenerator.KEY_DATE_TYPES[type.code]!!)
        }
        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(
            SentenceDetail(
                conditionalReleaseDate = LocalDate.of(2024, 1, 1),
                sentenceExpiryDate = LocalDate.of(2025, 1, 1)
            )
        )
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(
            PersonGenerator.DEFAULT
        )
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(
            listOf(custody.id)
        )
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(crdsApiClient, never()).getOperativeSentenceEnvelope(any())
        verify(disposalRepository, never()).save(any<Disposal>())
        verify(keyDateRepository).saveAll(
            check<List<KeyDate>> { saved ->
                assertThat(
                    saved.any { it.type.code == CustodyDateType.ELECTRONIC_MONITORING_END_DATE.code },
                    equalTo(false)
                )
                assertThat(saved.any { it.type.code == CustodyDateType.FINAL_THIRD_START_DATE.code }, equalTo(false))
            })
    }

    @Test
    fun `SDS+ flag null defaults to regular SDS calculation for EM end date`() {
        featureFlagEnabled(false)
        listOf(
            CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE,
            CustodyDateType.SENTENCE_EXPIRY_DATE,
            CustodyDateType.SUSPENSION_DATE_IF_RESET,
            CustodyDateType.ELECTRONIC_MONITORING_END_DATE,
            CustodyDateType.FINAL_THIRD_START_DATE
        ).forEach { type ->
            whenever(
                referenceDataRepository.findByDatasetAndCode(
                    DatasetCode.KEY_DATE_TYPE,
                    type.code
                )
            ).thenReturn(ReferenceDataGenerator.KEY_DATE_TYPES[type.code]!!)
        }

        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val disposal = generateDisposal(generateEvent())
        val custody = generateCustodialSentence(disposal = disposal, bookingRef = booking.bookingNo)

        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(
            SentenceDetail(
                conditionalReleaseDate = LocalDate.of(2024, 1, 1),
                sentenceExpiryDate = LocalDate.of(2025, 1, 1)
            )
        )
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(
            PersonGenerator.DEFAULT
        )
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(
            listOf(custody.id)
        )
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)
        whenever(crdsApiClient.getOperativeSentenceEnvelope(booking.offenderNo)).thenReturn(
            OperativeSentenceEnvelope(
                sentenceEnvelopeLengthInDays = 50L,
                containsAnSDSPlusSentence = null,
                bookingId = booking.id
            )
        )
        whenever(custodyRepository.findAllSentencesByPersonId(PersonGenerator.DEFAULT.id)).thenReturn(listOf(disposal))

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        assertNull(custody.disposal.sdsPlus)
        verify(disposalRepository).save(
            check<Disposal> {
                assertNull(it.sdsPlus)
            }
        )
        verify(keyDateRepository).saveAll(
            check<List<KeyDate>> { saved ->
                val emed = saved.single { it.type.code == CustodyDateType.ELECTRONIC_MONITORING_END_DATE.code }
                assertThat(emed.date, equalTo(LocalDate.of(2024, 1, 4)))
            }
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `SDS+ flag is preserved and final third date removed when CRDS API returns 404`(
        calculateDatesFromDelius: Boolean
    ) {
        featureFlagEnabled(calculateDatesFromDelius)
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val disposal = generateDisposal(generateEvent(), sdsPlus = true)
        val custody = generateCustodialSentence(disposal = disposal, bookingRef = booking.bookingNo)
        listOf(
            CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE,
            CustodyDateType.SENTENCE_EXPIRY_DATE,
            CustodyDateType.SUSPENSION_DATE_IF_RESET,
        ).forEach { type ->
            whenever(
                referenceDataRepository.findByDatasetAndCode(
                    DatasetCode.KEY_DATE_TYPE,
                    type.code
                )
            ).thenReturn(ReferenceDataGenerator.KEY_DATE_TYPES[type.code]!!)
        }
        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(
            SentenceDetail(
                conditionalReleaseDate = LocalDate.of(2024, 1, 1),
                sentenceExpiryDate = LocalDate.of(2025, 1, 1)
            )
        )
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(
            PersonGenerator.DEFAULT
        )
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(
            listOf(custody.id)
        )
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)
        whenever(crdsApiClient.getOperativeSentenceEnvelope(booking.offenderNo))
            .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "not found", HttpHeaders(), null, null))
        whenever(custodyRepository.findAllSentencesByPersonId(PersonGenerator.DEFAULT.id)).thenReturn(listOf(disposal))
        whenever(
            keyDateRepository.deleteByCustodyDisposalIdAndTypeCode(
                disposal.id,
                CustodyDateType.FINAL_THIRD_START_DATE.code
            )
        ).thenReturn(1L)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        assertThat(custody.disposal.sdsPlus, equalTo(true))
        verify(custodyRepository).findAllSentencesByPersonId(PersonGenerator.DEFAULT.id)
        verifyNoInteractions(disposalRepository)
        verify(keyDateRepository).deleteByCustodyDisposalIdAndTypeCode(
            disposal.id, CustodyDateType.FINAL_THIRD_START_DATE.code
        )
        verify(telemetryService).trackEvent(
            eq("SdsPlusFlagUpdated"),
            eq(
                mapOf(
                    "crn" to PersonGenerator.DEFAULT.crn,
                    "nomsNumber" to PersonGenerator.DEFAULT.nomsId,
                    "eventNumber" to disposal.event.eventNumber,
                    "sdsPlus" to "true",
                    "sdsPlusBefore" to "true",
                    "sdsPlusChanged" to "false",
                    "finalThirdRemoved" to "1"
                )
            ),
            any()
        )
        verify(keyDateRepository).saveAll(
            check<List<KeyDate>> { saved ->
                assertThat(
                    saved.any { it.type.code == CustodyDateType.ELECTRONIC_MONITORING_END_DATE.code },
                    equalTo(false)
                )
                assertThat(saved.any { it.type.code == CustodyDateType.FINAL_THIRD_START_DATE.code }, equalTo(false))
            })
    }

    @Test
    fun `Feature flag enabled uses notional end date only for final third when CRD and SED are missing`() {
        featureFlagEnabled(true)
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                DatasetCode.KEY_DATE_TYPE,
                CustodyDateType.FINAL_THIRD_START_DATE.code
            )
        ).thenReturn(ReferenceDataGenerator.KEY_DATE_TYPES[CustodyDateType.FINAL_THIRD_START_DATE.code]!!)
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val disposal = generateDisposal(
            generateEvent(),
            notionalEndDate = LocalDate.of(2025, 1, 1),
            lengthInDays = 366L
        )
        val custody = generateCustodialSentence(disposal = disposal, bookingRef = booking.bookingNo)
        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(SentenceDetail())
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(
            PersonGenerator.DEFAULT
        )
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(
            listOf(custody.id)
        )
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)
        whenever(custodyRepository.findAllSentencesByPersonId(PersonGenerator.DEFAULT.id)).thenReturn(listOf(disposal))
        whenever(crdsApiClient.getOperativeSentenceEnvelope(booking.offenderNo)).thenReturn(
            OperativeSentenceEnvelope(
                bookingId = booking.id,
                containsAnSDSPlusSentence = false,
                sentenceEnvelopeLengthInDays = 50L
            )
        )

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(crdsApiClient).getOperativeSentenceEnvelope(booking.offenderNo)
        assertThat(disposal.sdsPlus, equalTo(false))
        verify(disposalRepository).save(disposal)
        verify(keyDateRepository, never()).deleteByCustodyDisposalIdAndTypeCode(any(), any())
        verify(keyDateRepository).saveAll(
            check<List<KeyDate>> { saved ->
                assertThat(
                    saved.any { it.type.code == CustodyDateType.ELECTRONIC_MONITORING_END_DATE.code },
                    equalTo(false)
                )
                assertThat(
                    saved.single { it.type.code == CustodyDateType.FINAL_THIRD_START_DATE.code }.date,
                    equalTo(LocalDate.of(2024, 9, 1))
                )
            }
        )
    }

    @Test
    fun `Feature flag enabled does not create final third date when disposal sds plus is true`() {
        featureFlagEnabled(true)
        listOf(
            CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE,
            CustodyDateType.ELECTRONIC_MONITORING_END_DATE,
        ).forEach { type ->
            whenever(
                referenceDataRepository.findByDatasetAndCode(DatasetCode.KEY_DATE_TYPE, type.code)
            ).thenReturn(ReferenceDataGenerator.KEY_DATE_TYPES[type.code]!!)
        }
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val disposal = generateDisposal(
            generateEvent(),
            notionalEndDate = LocalDate.of(2025, 1, 1),
            sdsPlus = true,
            lengthInDays = 366L
        )
        val custody = generateCustodialSentence(disposal = disposal, bookingRef = booking.bookingNo)
        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(
            SentenceDetail(conditionalReleaseDate = LocalDate.of(2024, 1, 1))
        )
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(
            PersonGenerator.DEFAULT
        )
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(
            listOf(custody.id)
        )
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)
        whenever(custodyRepository.findAllSentencesByPersonId(PersonGenerator.DEFAULT.id)).thenReturn(listOf(disposal))
        whenever(crdsApiClient.getOperativeSentenceEnvelope(booking.offenderNo)).thenReturn(
            OperativeSentenceEnvelope(
                bookingId = booking.id,
                containsAnSDSPlusSentence = true,
                sentenceEnvelopeLengthInDays = 50L
            )
        )

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(crdsApiClient).getOperativeSentenceEnvelope(booking.offenderNo)
        assertThat(disposal.sdsPlus, equalTo(true))
        verify(disposalRepository).save(disposal)
        verify(keyDateRepository).deleteByCustodyDisposalIdAndTypeCode(
            disposal.id, CustodyDateType.FINAL_THIRD_START_DATE.code
        )
        verify(keyDateRepository).saveAll(
            check<List<KeyDate>> { saved ->
                assertThat(saved.any { it.type.code == CustodyDateType.FINAL_THIRD_START_DATE.code }, equalTo(false))
                assertThat(
                    saved.single { it.type.code == CustodyDateType.ELECTRONIC_MONITORING_END_DATE.code }.date,
                    equalTo(LocalDate.of(2024, 3, 3))
                )
            }
        )
    }

    @Test
    fun `Feature flag enabled uses disposal length and CRD for EMED and SED for final third`() {
        featureFlagEnabled(true)
        listOf(
            CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE,
            CustodyDateType.SUSPENSION_DATE_IF_RESET,
            CustodyDateType.ELECTRONIC_MONITORING_END_DATE,
            CustodyDateType.FINAL_THIRD_START_DATE,
        ).forEach { type ->
            whenever(
                referenceDataRepository.findByDatasetAndCode(
                    DatasetCode.KEY_DATE_TYPE,
                    type.code
                )
            ).thenReturn(ReferenceDataGenerator.KEY_DATE_TYPES[type.code]!!)
        }
        val booking = Booking(127, "FG37K", true, PersonGenerator.DEFAULT.nomsId!!)
        val disposal = generateDisposal(
            generateEvent(),
            lengthInDays = 820L
        )
        val custody = generateCustodialSentence(disposal = disposal, bookingRef = booking.bookingNo)
        whenever(prisonApi.getSentenceDetail(booking.id)).thenReturn(
            SentenceDetail(
                conditionalReleaseDate = LocalDate.of(2025, 1, 1),
                sentenceExpiryDate = LocalDate.of(2027, 3, 12)
            )
        )
        whenever(prisonApi.getBooking(booking.id, basicInfo = false, extraInfo = true)).thenReturn(booking)
        whenever(personRepository.findByNomsIdIgnoreCaseAndSoftDeletedIsFalse(booking.offenderNo)).thenReturn(
            PersonGenerator.DEFAULT
        )
        whenever(custodyRepository.findCustodyId(PersonGenerator.DEFAULT.id, booking.bookingNo)).thenReturn(
            listOf(
                custody.id
            )
        )
        whenever(custodyRepository.findForUpdate(custody.id)).thenReturn(custody.id)
        whenever(custodyRepository.findCustodyById(custody.id)).thenReturn(custody)
        whenever(custodyRepository.findAllSentencesByPersonId(PersonGenerator.DEFAULT.id)).thenReturn(listOf(disposal))
        whenever(crdsApiClient.getOperativeSentenceEnvelope(booking.offenderNo)).thenReturn(
            OperativeSentenceEnvelope(
                bookingId = booking.id,
                containsAnSDSPlusSentence = false,
                sentenceEnvelopeLengthInDays = 50L
            )
        )
        whenever(
            referenceDataRepository.findByDatasetAndCode(
                DatasetCode.KEY_DATE_TYPE,
                CustodyDateType.SENTENCE_EXPIRY_DATE.code
            )
        )
            .thenReturn(ReferenceDataGenerator.KEY_DATE_TYPES[CustodyDateType.SENTENCE_EXPIRY_DATE.code]!!)

        custodyDateUpdateService.updateCustodyKeyDates(bookingId = booking.id)

        verify(crdsApiClient).getOperativeSentenceEnvelope(booking.offenderNo)
        assertThat(disposal.sdsPlus, equalTo(false))
        verify(disposalRepository).save(disposal)
        verify(keyDateRepository).saveAll(
            check<List<KeyDate>> { saved ->
                assertThat(
                    saved.single { it.type.code == CustodyDateType.FINAL_THIRD_START_DATE.code }.date,
                    equalTo(LocalDate.of(2026, 6, 11))
                )
                assertThat(
                    saved.single { it.type.code == CustodyDateType.ELECTRONIC_MONITORING_END_DATE.code }.date,
                    equalTo(LocalDate.of(2025, 2, 27))
                )
            }
        )
    }
}

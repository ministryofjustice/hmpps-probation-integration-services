package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.anyMap
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.client.*
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Equality
import uk.gov.justice.digital.hmpps.integrations.delius.entity.EventRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.EventService
import uk.gov.justice.digital.hmpps.service.InsertEventResult
import uk.gov.justice.digital.hmpps.service.InsertPersonResult
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var converter: NotificationConverter<CommonPlatformHearing>

    @Mock
    lateinit var personService: PersonService

    @Mock
    lateinit var eventService: EventService

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var probationSearchClient: ProbationSearchClient

    @Mock
    lateinit var notifier: Notifier

    @Mock
    private lateinit var featureFlags: FeatureFlags

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `inserts records when probation search match is not found`() {
        personIsSuccessfullyCreated()

        val hearingAppearance = CourtAppearanceGenerator.TRIAL_ADJOURNMENT
        val futureAppearance = CourtAppearanceGenerator.TRIAL_ADJOURNMENT_NO_HEARING
        whenever(eventService.insertEvent(any(), any(), any(), any(), any(), any())).thenReturn(
            InsertEventResult(
                EventGenerator.DEFAULT,
                MainOffenceGenerator.DEFAULT,
                listOf(hearingAppearance, futureAppearance),
                listOf(ContactGenerator.EAPP, ContactGenerator.EAPP),
                OrderManagerGenerator.DEFAULT
            )
        )
        whenever(eventRepository.findEventByCaseUrnAndCrn(any(), any())).thenReturn(null)
        whenever(eventRepository.findActiveEventsExcludingCaseUrn(any(), any())).thenReturn(emptyList())

        probationSearchMatchNotFound()
        featureFlagIsEnabled(true)

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(eq("EventCreated"), anyMap(), anyMap())
        verify(personService).insertPerson(any(), any())
        verify(notifier).caseCreated(any())
        verify(notifier).addressCreated(any())
    }

    @Test
    fun `does not insert person or address when match is found`() {
        probationSearchMatchFound()
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(personService, never()).insertPerson(any(), any())
        verify(notifier, never()).caseCreated(any())
        verify(notifier, never()).addressCreated(any())
    }

    @Test
    fun `When defendants with remanded in custody are not found then no inserts occur`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NO_REMAND)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(personService, never()).insertPerson(any(), any())
        verify(eventService, never()).insertEvent(any(), any(), any(), any(), any(), any())
        verify(eventService, never()).insertCourtAppearance(any(), any(), any(), any(), any())
        verify(notifier, never()).caseCreated(any())
        verify(notifier, never()).addressCreated(any())
    }

    @Test
    fun `When a defendant is missing name or dob then records are not inserted and probation-search is not performed`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NULL_FIELDS)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(probationSearchClient, never()).match(any())
        verify(personService, never()).insertPerson(any(), any())
        verify(notifier, never()).caseCreated(any())
        verify(notifier, never()).addressCreated(any())
    }

    @Test
    fun `Person created logged when feature flag enabled`() {
        probationSearchMatchNotFound()
        featureFlagIsEnabled(true)
        personIsSuccessfullyCreated()

        val hearingAppearance = CourtAppearanceGenerator.TRIAL_ADJOURNMENT
        val futureAppearance = CourtAppearanceGenerator.TRIAL_ADJOURNMENT_NO_HEARING
        whenever(eventService.insertEvent(any(), any(), any(), any(), any(), any())).thenReturn(
            InsertEventResult(
                EventGenerator.DEFAULT,
                MainOffenceGenerator.DEFAULT,
                listOf(hearingAppearance, futureAppearance),
                listOf(ContactGenerator.EAPP, ContactGenerator.EAPP),
                OrderManagerGenerator.DEFAULT
            )
        )
        whenever(eventRepository.findEventByCaseUrnAndCrn(any(), any())).thenReturn(null)
        whenever(eventRepository.findActiveEventsExcludingCaseUrn(any(), any())).thenReturn(emptyList())

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(eq("PersonCreated"), anyMap(), anyMap())
        verify(personService).insertPerson(any(), any())
        verify(eventService).insertEvent(any(), any(), any(), any(), any(), any())
        verify(notifier).caseCreated(any())
        verify(notifier).addressCreated(any())
    }

    @Test
    fun `Simulated person created logged when feature flag disabled`() {
        probationSearchMatchNotFound()
        featureFlagIsEnabled(false)
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(eq("SimulatedPersonCreated"), anyMap(), anyMap())
        verify(personService, never()).insertPerson(any(), any())
        verify(notifier, never()).caseCreated(any())
        verify(notifier, never()).addressCreated(any())
    }

    @Test
    fun `updates event when existing event with case urn found`() {
        personIsSuccessfullyCreated()

        val event = EventGenerator.DEFAULT
        whenever(eventRepository.findEventByCaseUrnAndCrn(any(), any())).thenReturn(event)
        whenever(eventRepository.findActiveEventsExcludingCaseUrn(any(), any())).thenReturn(emptyList())

        probationSearchMatchNotFound()
        featureFlagIsEnabled(true)

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(eventService, never()).insertEvent(any(), any(), any(), any(), any(), any())
        verify(eventService, never()).insertCourtAppearance(any(), any(), any(), any(), any())
        verify(telemetryService).trackEvent(eq("SimulatedUpdateEvent"), anyMap(), anyMap())
    }

    @Test
    fun `skips update event when other active events are found`() {
        personIsSuccessfullyCreated()

        val event = EventGenerator.DEFAULT
        whenever(eventRepository.findEventByCaseUrnAndCrn(any(), any())).thenReturn(event)
        whenever(eventRepository.findActiveEventsExcludingCaseUrn(any(), any()))
            .thenReturn(listOf(EventGenerator.DEFAULT))

        probationSearchMatchNotFound()
        featureFlagIsEnabled(true)

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(eventService, never()).insertEvent(any(), any(), any(), any(), any(), any())
        verify(eventService, never()).insertCourtAppearance(any(), any(), any(), any(), any())
        verify(telemetryService).trackEvent(eq("EventUpdateSkipped"), anyMap(), anyMap())
    }

    @Test
    fun `skips create event when other active events are found`() {
        personIsSuccessfullyCreated()

        whenever(eventRepository.findEventByCaseUrnAndCrn(any(), any())).thenReturn(null)
        whenever(eventRepository.findActiveEventsExcludingCaseUrn(any(), any()))
            .thenReturn(listOf(EventGenerator.DEFAULT))

        probationSearchMatchNotFound()
        featureFlagIsEnabled(true)

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(eventService, never()).insertEvent(any(), any(), any(), any(), any(), any())
        verify(eventService, never()).insertCourtAppearance(any(), any(), any(), any(), any())
        verify(telemetryService).trackEvent(eq("EventCreatedSkipped"), anyMap(), anyMap())
    }

    private fun featureFlagIsEnabled(flag: Boolean) {
        whenever(featureFlags.enabled("common-platform-record-creation-toggle")).thenReturn(flag)
    }

    private fun personIsSuccessfullyCreated() {
        whenever(personService.insertPerson(any(), any())).thenReturn(
            InsertPersonResult(
                person = PersonGenerator.DEFAULT,
                personManager = PersonManagerGenerator.DEFAULT,
                equality = Equality(id = 1L, personId = 1L, softDeleted = false),
                address = PersonAddressGenerator.MAIN_ADDRESS,
            )
        )
    }

    private fun probationSearchMatchNotFound() {
        whenever(probationSearchClient.match(any())).thenReturn(
            ProbationMatchResponse(
                matches = emptyList(),
                matchedBy = "NONE"
            )
        )
    }

    private fun probationSearchMatchFound() {
        val fakeMatchResponse = ProbationMatchResponse(
            matches = listOf(
                OffenderMatch(
                    offender = OffenderDetail(
                        otherIds = IDs(crn = "X123456", pncNumber = "00000000000Z"),
                        firstName = "Name",
                        surname = "Name",
                        dateOfBirth = LocalDate.of(1980, 1, 1)
                    )
                )
            ),
            matchedBy = "PNC"
        )
        whenever(probationSearchClient.match(any())).thenReturn(fakeMatchResponse)
    }
}

package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.anyMap
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.dto.InsertEventResult
import uk.gov.justice.digital.hmpps.dto.InsertPersonResult
import uk.gov.justice.digital.hmpps.dto.InsertRemandResult
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.client.CorePersonClient
import uk.gov.justice.digital.hmpps.integrations.client.CorePersonRecord
import uk.gov.justice.digital.hmpps.integrations.client.Identifiers
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.OffenceService
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.service.RemandService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class FIFOHandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var remandService: RemandService

    @Mock
    lateinit var converter: NotificationConverter<CommonPlatformHearing>

    @Mock
    lateinit var personService: PersonService

    @Mock
    lateinit var offenceService: OffenceService

    @Mock
    private lateinit var featureFlags: FeatureFlags

    @Mock
    lateinit var notifier: Notifier

    @Mock
    lateinit var corePersonClient: CorePersonClient

    @InjectMocks
    lateinit var handler: FIFOHandler

    @Test
    fun `inserts records when probation search match is not found`() {
        personOnRemandIsSuccessfullyCreated()

        corePersonHasNoCrn()
        featureFlagIsEnabled(true)
        whenever(corePersonClient.createPersonRecord(any(), any())).thenReturn(ResponseEntity.status(500).build())

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(remandService).insertPersonOnRemand(any())
    }

    @Test
    fun `does not insert person or address when match is found`() {
        corePersonAlreadyHasCrn()
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(remandService, never()).insertPersonOnRemand(any())
    }

    @Test
    fun `When defendants with remanded in custody are not found then no inserts occur`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NO_REMAND)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(remandService, never()).insertPersonOnRemand(any())
    }

    @Test
    fun `When a defendant is missing name or dob then records are not inserted and probation-search is not performed`() {
        corePersonHasNoCrn()
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NULL_FIELDS)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
        verify(remandService, never()).insertPersonOnRemand(any())
    }

    @Test
    fun `Simulated person created logged when feature flag disabled`() {
        corePersonHasNoCrn()
        featureFlagIsEnabled(false)
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(eq("SimulatedPersonCreated"), anyMap(), anyMap())
        verify(remandService, never()).insertPersonOnRemand(any())
    }

    @Test
    fun `Messages with future hearing dates are flagged correctly in app insights`() {
        corePersonAlreadyHasCrn()
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_FUTURE_HEARING_DATES)
        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(
            "PersonAlreadyExists",
            mapOf(
                "defendantId" to "00000000-0000-0000-0000-000000000000",
                "crns" to "[X123456]",
                "hearingId" to notification.message.hearing.id,
                "hearingDates" to "2024-01-01T12:00Z[Europe/London], 2024-03-19T12:00Z[Europe/London], 2030-12-31T12:00Z[Europe/London]",
                "futureHearingDate" to "true"
            ),
            mapOf()
        )
    }

    @Test
    fun `domain event notifications sent when person record is created successfully`() {
        personOnRemandIsSuccessfullyCreated()

        corePersonHasNoCrn()
        featureFlagIsEnabled(true)
        whenever(corePersonClient.createPersonRecord(any(), any())).thenReturn(ResponseEntity.status(500).build())

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        handler.handle(notification)

        verify(telemetryService).trackEvent(eq("PersonCreated"), anyMap(), anyMap())
        verify(telemetryService).trackEvent(eq("EventCreated"), anyMap(), anyMap())
        verify(notifier).caseCreated(any())
        verify(notifier).addressCreated(any())
    }

    private fun featureFlagIsEnabled(flag: Boolean) {
        whenever(featureFlags.enabled("common-platform-record-creation-toggle")).thenReturn(flag)
    }

    private fun personOnRemandIsSuccessfullyCreated() {
        whenever(offenceService.findMainOffence(any())).thenReturn(
            HearingOffence(
                id = "0",
                offenceTitle = "Offence",
                wording = "Offence",
                offenceCode = "AA00000",
                offenceLegislation = "",
                listingNumber = 0
            )
        )

        whenever(remandService.insertPersonOnRemand(any())).thenReturn(
            InsertRemandResult(
                InsertPersonResult(
                    person = PersonGenerator.DEFAULT,
                    personManager = PersonManagerGenerator.DEFAULT,
                    equality = EqualityGenerator.DEFAULT,
                    address = PersonAddressGenerator.MAIN_ADDRESS,
                ),
                InsertEventResult(
                    EventGenerator.DEFAULT,
                    MainOffenceGenerator.DEFAULT,
                    CourtAppearanceGenerator.TRIAL_ADJOURNMENT,
                    ContactGenerator.EAPP,
                    OrderManagerGenerator.DEFAULT
                )
            )
        )
    }

    private fun corePersonHasNoCrn() {
        val person = CorePersonRecord(
            "John", "Robert", "Smith",
            LocalDate.now().minusYears(21),
            Identifiers()
        )
        whenever(corePersonClient.findByDefendantId(any())).thenReturn(person)
    }

    private fun corePersonAlreadyHasCrn() {
        val person = CorePersonRecord(
            "John", "Robert", "Smith", LocalDate.now().minusYears(21),
            Identifiers(crns = listOf("X123456"))
        )
        whenever(corePersonClient.findByDefendantId(any())).thenReturn(person)
    }
}

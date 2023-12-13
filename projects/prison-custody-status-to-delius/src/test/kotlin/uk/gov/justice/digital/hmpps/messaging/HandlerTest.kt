package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {

    @Mock
    lateinit var featureFlags: FeatureFlags

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    lateinit var prisonApiClient: PrisonApiClient

    @Mock
    lateinit var actionProcessor: ActionProcessor

    private val configs = PrisonerMovementConfigs(
        listOf(
            PrisonerMovementConfig(
                listOf(PrisonerMovement.Type.ADMISSION),
                actionNames = listOf("Recall", "UpdateStatus", "UpdateLocation")
            ),
            PrisonerMovementConfig(
                listOf(PrisonerMovement.Type.RELEASED_TO_HOSPITAL),
                actionNames = listOf("Recall", "UpdateStatus", "UpdateLocation"),
                featureFlag = "messages_released_hospital"
            )
        )
    )

    lateinit var handler: Handler

    @BeforeEach
    fun setup() {
        handler = Handler(configs, featureFlags, telemetryService, prisonApiClient, actionProcessor, converter)
    }

    private val notification = Notification(
        message = HmppsDomainEvent(
            "prison-offender-events.prisoner.released",
            1,
            "https//detail/url",
            ZonedDateTime.now(),
            nullableAdditionalInformation = AdditionalInformation(
                mutableMapOf(
                    "nomsNumber" to "Z0001ZZ",
                    "prisonId" to "ZZZ",
                    "reason" to "RETURN_FROM_COURT",
                    "nomisMovementReasonCode" to "OPA",
                    "details" to "CRT-OPA"
                )
            ),
            personReference = PersonReference(listOf(PersonIdentifier("NOMS", "Z0001ZZ")))
        ),
        attributes = MessageAttributes("prison-offender-events.prisoner.released")
    )

    private val booking = Booking(
        1208804,
        "45680A",
        true,
        "A5295DZ",
        "SWI",
        "ADM",
        "INT",
        Booking.InOutStatus.IN
    )

    private val identifierAddedNotification = notification.copy(
        message = HmppsDomainEvent(
            eventType = "probation-case.prison-identifier.added",
            version = 1,
            personReference = PersonReference(listOf(PersonIdentifier("NOMS", booking.personReference)))
        ),
        attributes = MessageAttributes("probation-case.prison-identifier.added")
    )

    @Test
    fun unknownMessagesAreThrown() {
        assertThrows<IllegalArgumentException> {
            handler.handle(
                notification.copy(
                    message = notification.message.copy(eventType = "unknown"),
                    attributes = MessageAttributes("unknown")
                )
            )
        }
    }

    @Test
    fun `messages with inactive booking are ignored`() {
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(booking.copy(active = false))
        handler.handle(identifierAddedNotification)
        verify(telemetryService).trackEvent(
            "BookingInactive",
            mapOf(
                "occurredAt" to identifierAddedNotification.message.occurredAt.toString(),
                "nomsNumber" to booking.personReference
            )
        )
    }

    @Test
    fun `logs telemetry event when unknown reason given`() {
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference))
            .thenReturn(booking.copy(movementType = "UNKNOWN", movementReason = ""))
        handler.handle(identifierAddedNotification)
        verify(telemetryService).trackEvent(
            "UnableToCalculateMovementType",
            mapOf(
                "occurredAt" to identifierAddedNotification.message.occurredAt.toString(),
                "nomsNumber" to booking.personReference,
                "movementType" to "UNKNOWN",
                "movementReason" to ""
            )
        )
    }

    @Test
    fun `logs telemetry event when no config for movement`() {
        val nomsId = "Z0001ZZ"
        val prisonId = "ZZZ"
        val movementReason = "OPA"

        val booking = booking.copy(
            personReference = nomsId,
            agencyId = prisonId,
            movementType = "CRT",
            movementReason = movementReason
        )
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(booking)

        handler.handle(notification)
        verify(telemetryService).trackEvent(
            "NoConfigForMovement",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to nomsId,
                "institution" to prisonId,
                "details" to "CRT-OPA",
                "movementType" to "RETURN_FROM_COURT",
                "movementReason" to movementReason
            )
        )
    }

    @Test
    fun `booking is correctly mapped to a received prisoner movement when identifier added`() {
        val booking = booking.copy(movementReason = "N")
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(booking)
        whenever(actionProcessor.processActions(any(), any()))
            .thenReturn(
                listOf(
                    ActionResult.Success(
                        ActionResult.Type.Recalled,
                        booking.prisonerMovement(ZonedDateTime.now()).telemetryProperties()
                    )
                )
            )

        handler.handle(identifierAddedNotification)
        val received = argumentCaptor<PrisonerMovement.Received>()
        verify(actionProcessor).processActions(
            received.capture(),
            eq(listOf("Recall", "UpdateStatus", "UpdateLocation"))
        )
        assertThat(received.firstValue.nomsId, equalTo("A5295DZ"))
        assertThat(received.firstValue.reason, equalTo("N"))
        assertThat(received.firstValue.type, equalTo(PrisonerMovement.Type.ADMISSION))
        assertThat(received.firstValue.prisonId, equalTo("SWI"))
    }

    @Test
    fun `if booking is released when identifier added - telemetry is tracked`() {
        whenever(featureFlags.enabled("messages_released_hospital")).thenReturn(true)
        val releaseBooking = booking.copy(
            movementType = "REL",
            movementReason = "HQ",
            inOutStatus = Booking.InOutStatus.OUT
        )
        val prisonerMovement = releaseBooking.prisonerMovement(identifierAddedNotification.message.occurredAt)
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(releaseBooking)
        whenever(actionProcessor.processActions(any(), any()))
            .thenReturn(
                listOf(
                    ActionResult.Success(
                        ActionResult.Type.Recalled,
                        prisonerMovement.telemetryProperties()
                    ),
                    ActionResult.Success(
                        ActionResult.Type.LocationUpdated,
                        prisonerMovement.telemetryProperties()
                    ),
                    ActionResult.Success(
                        ActionResult.Type.StatusUpdated,
                        prisonerMovement.telemetryProperties()
                    )
                )
            )

        handler.handle(identifierAddedNotification)
        val released = argumentCaptor<PrisonerMovement.Released>()
        verify(actionProcessor).processActions(
            released.capture(),
            eq(listOf("Recall", "UpdateStatus", "UpdateLocation"))
        )
        verify(telemetryService).trackEvent(
            "Recalled",
            prisonerMovement.telemetryProperties(),
            mapOf()
        )
        verify(telemetryService).trackEvent(
            "LocationUpdated",
            prisonerMovement.telemetryProperties(),
            mapOf()
        )
        verify(telemetryService).trackEvent(
            "StatusUpdated",
            prisonerMovement.telemetryProperties(),
            mapOf()
        )
    }

    @Test
    fun `noop whenever feature flagged and not active`() {
        val nomsId = "Z0001ZZ"
        val prisonId = "ZZZ"
        val movementReason = "HQ"

        val booking = booking.copy(
            personReference = nomsId,
            agencyId = prisonId,
            movementType = "REL",
            movementReason = movementReason,
            inOutStatus = Booking.InOutStatus.OUT
        )
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(booking)
        whenever(featureFlags.enabled("messages_released_hospital")).thenReturn(false)

        val hospitalNotification = notification.copy(
            message = notification.message.copy(
                nullableAdditionalInformation = AdditionalInformation(mutableMapOf("nomsNumber" to nomsId))
            )
        )
        handler.handle(hospitalNotification)
        verify(telemetryService).trackEvent(
            "FeatureFlagNotActive",
            mapOf(
                "featureFlag" to "messages_released_hospital",
                "occurredAt" to hospitalNotification.message.occurredAt.toString(),
                "nomsNumber" to nomsId,
                "reason" to "RELEASED_TO_HOSPITAL",
                "movementReason" to "HQ",
                "movementType" to "Released"
            )
        )
    }
}

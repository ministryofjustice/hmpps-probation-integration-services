package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.BookingId
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {

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
                actionNames = listOf("Recall", "UpdateStatus", "UpdateLocation")
            )
        )
    )

    lateinit var handler: Handler

    @BeforeEach
    fun setup() {
        handler = Handler(configs, telemetryService, prisonApiClient, actionProcessor, converter)
    }

    private val notification = Notification(
        message = HmppsDomainEvent(
            "prison-offender-events.prisoner.released",
            1,
            "https//detail/url",
            ZonedDateTime.now(),
            additionalInformation = AdditionalInformation(
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
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(BookingId(booking.id))
        whenever(prisonApiClient.getBooking(booking.id)).thenReturn(booking.copy(active = false))
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
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(BookingId(booking.id))
        whenever(prisonApiClient.getBooking(booking.id))
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
        handler.handle(notification)
        verify(telemetryService).trackEvent(
            "NoConfigForMovement",
            mapOf(
                "occurredAt" to notification.message.occurredAt.toString(),
                "nomsNumber" to "Z0001ZZ",
                "institution" to "ZZZ",
                "details" to "CRT-OPA",
                "movementType" to "RETURN_FROM_COURT",
                "movementReason" to "OPA"
            )
        )
    }

    @Test
    fun `booking is correctly mapped to a received prisoner movement when identifier added`() {
        val booking = booking.copy(movementReason = "N")
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(BookingId(booking.id))
        whenever(prisonApiClient.getBooking(booking.id)).thenReturn(booking)
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
        val releaseBooking = booking.copy(
            movementType = "REL",
            movementReason = "HQ",
            inOutStatus = Booking.InOutStatus.OUT
        )
        val prisonerMovement = releaseBooking.prisonerMovement(identifierAddedNotification.message.occurredAt)
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(BookingId(booking.id))
        whenever(prisonApiClient.getBooking(booking.id)).thenReturn(releaseBooking)
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
}

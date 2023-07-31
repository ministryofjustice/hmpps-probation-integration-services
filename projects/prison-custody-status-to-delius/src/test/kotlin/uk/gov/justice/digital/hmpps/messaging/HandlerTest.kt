package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallService
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseService
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttribute
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.ZonedDateTime

@Suppress("UNCHECKED_CAST")
@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var releaseService: ReleaseService

    @Mock
    lateinit var recallService: RecallService

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    lateinit var prisonApiClient: PrisonApiClient

    @InjectMocks
    lateinit var handler: Handler

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
                    "reason" to "Test data",
                    "nomisMovementReasonCode" to "OPA",
                    "details" to "Test data"
                )
            )
        ),
        attributes = MessageAttributes("prison-offender-events.prisoner.released")
    )

    private val poe = Notification(
        CustodialStatusChanged(1208804, 1, null, ZonedDateTime.now()),
        MessageAttributes("EXTERNAL_MOVEMENT_RECORD-INSERTED")
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

    @Test
    fun messageIsLoggedToTelemetry() {
        whenever(
            releaseService.release(
                PrisonerMovement.Released(
                    notification.message.additionalInformation.nomsNumber(),
                    notification.message.additionalInformation.prisonId(),
                    notification.message.additionalInformation.reason(),
                    notification.message.additionalInformation.movementReason(),
                    notification.message.occurredAt
                )
            )
        ).thenReturn(ReleaseOutcome.PrisonerReleased)
        handler.handle(notification as Notification<Any>)
        verify(telemetryService).notificationReceived(notification)
    }

    @Test
    fun releaseMessagesAreHandled() {
        whenever(
            releaseService.release(
                PrisonerMovement.Released(
                    notification.message.additionalInformation.nomsNumber(),
                    notification.message.additionalInformation.prisonId(),
                    notification.message.additionalInformation.reason(),
                    notification.message.additionalInformation.movementReason(),
                    notification.message.occurredAt
                )
            )
        ).thenReturn(ReleaseOutcome.PrisonerReleased)

        handler.handle(notification as Notification<Any>)
        verify(releaseService).release(
            PrisonerMovement.Released(
                notification.message.additionalInformation.nomsNumber(),
                notification.message.additionalInformation.prisonId(),
                notification.message.additionalInformation.reason(),
                notification.message.additionalInformation.movementReason(),
                notification.message.occurredAt
            )
        )
    }

    @Test
    fun recallMessagesAreHandled() {
        whenever(
            recallService.recall(
                PrisonerMovement.Received(
                    "Z0001ZZ",
                    "ZZZ",
                    "Test data",
                    "OPA",
                    notification.message.occurredAt
                )
            )
        ).thenReturn(RecallOutcome.PrisonerRecalled)

        val attrs = MessageAttributes("prison-offender-events.prisoner.received")
        attrs["nomisMovementReasonCode"] = MessageAttribute("String", "R1")
        handler.handle(
            notification.copy(
                message = notification.message.copy(eventType = "prison-offender-events.prisoner.received"),
                attributes = attrs
            ) as Notification<Any>
        )
        verify(telemetryService).trackEvent("PrisonerRecalled", notification.message.telemetryProperties())
    }

    @Test
    fun unknownMessagesAreThrown() {
        assertThrows<IllegalArgumentException> {
            handler.handle(
                notification.copy(
                    message = notification.message.copy(eventType = "unknown"),
                    attributes = MessageAttributes("unknown")
                ) as Notification<Any>
            )
        }
    }

    @Test
    fun `messages with inactive booking are ignored`() {
        whenever(prisonApiClient.getBooking(poe.message.bookingId)).thenReturn(booking.copy(active = false))
        handler.handle(poe as Notification<Any>)
        verify(telemetryService).trackEvent("BookingInactive", poe.message.telemetryProperties())
    }

    @Test
    fun `ignores messages with imprisonment status sequence greater than 0`() {
        val notification = poe.copy(message = poe.message.copy(imprisonmentStatusSeq = 1))
        handler.handle(notification as Notification<Any>)
        verify(prisonApiClient, never()).getBooking(any(), any(), any())
        verify(telemetryService).trackEvent("DuplicateImprisonmentStatus", notification.message.telemetryProperties())
    }

    @Test
    fun `logs telemetry event when unknown reason given`() {
        whenever(prisonApiClient.getBooking(poe.message.bookingId))
            .thenReturn(booking.copy(movementType = "UNKNOWN", movementReason = ""))
        handler.handle(poe as Notification<Any>)
        verify(telemetryService).trackEvent(
            "UnableToCalculateMovementType",
            mapOf(
                "bookingId" to "1208804",
                "movementSeq" to "1",
                "movementType" to "UNKNOWN",
                "movementReason" to ""
            )
        )
    }

    @Test
    fun `booking is correctly mapped to a received prisoner movement`() {
        whenever(prisonApiClient.getBooking(poe.message.bookingId)).thenReturn(booking)
        whenever(recallService.recall(any())).thenReturn(RecallOutcome.PrisonerRecalled)

        handler.handle(poe as Notification<Any>)
        val received = argumentCaptor<PrisonerMovement.Received>()
        verify(recallService).recall(received.capture())
        assertThat(received.firstValue.nomsId, equalTo("A5295DZ"))
        assertThat(received.firstValue.movementReason, equalTo("INT"))
        assertThat(received.firstValue.reason, equalTo("TRANSFERRED"))
        assertThat(received.firstValue.prisonId, equalTo("SWI"))
    }

    @Test
    fun `booking is correctly mapped to a released prisoner movement`() {
        whenever(prisonApiClient.getBooking(poe.message.bookingId))
            .thenReturn(
                booking.copy(
                    movementType = "REL",
                    movementReason = "HQ",
                    inOutStatus = Booking.InOutStatus.OUT
                )
            )
        whenever(releaseService.release(any())).thenReturn(ReleaseOutcome.PrisonerReleased)

        handler.handle(poe as Notification<Any>)
        val released = argumentCaptor<PrisonerMovement.Released>()
        verify(releaseService).release(released.capture())
        assertThat(released.firstValue.nomsId, equalTo("A5295DZ"))
        assertThat(released.firstValue.movementReason, equalTo("HQ"))
        assertThat(released.firstValue.reason, equalTo("RELEASE_TO_HOSPITAL"))
        assertThat(released.firstValue.prisonId, equalTo("SWI"))
    }
}

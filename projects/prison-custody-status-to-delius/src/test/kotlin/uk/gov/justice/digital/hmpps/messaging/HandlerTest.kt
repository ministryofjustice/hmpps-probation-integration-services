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
import uk.gov.justice.digital.hmpps.integrations.prison.BookingId
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttribute
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.ZonedDateTime

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
        handler.handle(notification)
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

        handler.handle(notification)
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
            )
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
                )
            )
        }
    }

    @Test
    fun `messages with inactive booking are ignored`() {
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(BookingId(booking.id))
        whenever(prisonApiClient.getBooking(booking.id)).thenReturn(booking.copy(active = false))
        handler.handle(identifierAddedNotification)
        verify(telemetryService).trackEvent("BookingInactive", mapOf("nomsId" to booking.personReference))
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
                "nomsId" to booking.personReference,
                "movementType" to "UNKNOWN",
                "movementReason" to ""
            )
        )
    }

    @Test
    fun `booking is correctly mapped to a received prisoner movement when identifier added`() {
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(BookingId(booking.id))
        whenever(prisonApiClient.getBooking(booking.id)).thenReturn(booking.copy(movementReason = "N"))
        whenever(recallService.recall(any())).thenReturn(RecallOutcome.PrisonerRecalled)

        handler.handle(identifierAddedNotification)
        val received = argumentCaptor<PrisonerMovement.Received>()
        verify(recallService).recall(received.capture())
        assertThat(received.firstValue.nomsId, equalTo("A5295DZ"))
        assertThat(received.firstValue.movementReason, equalTo("N"))
        assertThat(received.firstValue.reason, equalTo("ADMISSION"))
        assertThat(received.firstValue.prisonId, equalTo("SWI"))
    }

    @Test
    fun `if booking is released when identifier added - telemetry is tracked`() {
        val releaseBooking = booking.copy(
            movementType = "REL",
            movementReason = "HQ",
            inOutStatus = Booking.InOutStatus.OUT
        )
        whenever(prisonApiClient.getBookingByNomsId(booking.personReference)).thenReturn(BookingId(booking.id))
        whenever(prisonApiClient.getBooking(booking.id)).thenReturn(releaseBooking)

        handler.handle(identifierAddedNotification)
        verify(releaseService, never()).release(any())
        verify(telemetryService).trackEvent(
            "IdentifierAddedForReleasedPrisoner",
            releaseBooking.prisonerMovement(identifierAddedNotification.message.occurredAt).telemetryProperties(),
            mapOf()
        )
    }
}

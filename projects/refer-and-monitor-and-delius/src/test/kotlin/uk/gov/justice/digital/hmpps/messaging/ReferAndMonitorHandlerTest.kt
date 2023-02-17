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
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.SessionAppointmentSubmitted
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class ReferAndMonitorHandlerTest {

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var eventHandler: DomainEventHandler

    lateinit var handler: ReferAndMonitorHandler

    @BeforeEach
    fun setUp() {
        handler = ReferAndMonitorHandler(converter, telemetryService, listOf(eventHandler))
    }

    @Test
    fun `no detail url throws illegal argument exception`() {
        val unknownEventType = "unknown.event.type"
        val notification = Notification(
            HmppsDomainEvent(
                unknownEventType,
                1,
                occurredAt = ZonedDateTime.now(),
            )
        )

        val ex = assertThrows<IllegalArgumentException> { handler.handle(notification) }
        assertThat(ex.message, equalTo("Detail Url Missing"))
    }

    @Test
    fun `unhandled event type is recorded in telemetry`() {
        val unknownEventType = "unknown.event.type"
        val notification = Notification(
            HmppsDomainEvent(
                unknownEventType,
                1,
                occurredAt = ZonedDateTime.now(),
                detailUrl = "DetailUrl"
            ),
            MessageAttributes(unknownEventType)
        )

        handler.handle(notification)

        verify(telemetryService).trackEvent(
            eq("UnhandledEventReceived"),
            eq(mapOf("eventType" to unknownEventType)),
            any()
        )
    }

    @Test
    fun `message event type used if no notification event type`() {
        val notification = Notification(
            HmppsDomainEvent(
                SessionAppointmentSubmitted.name,
                1,
                occurredAt = ZonedDateTime.now(),
                detailUrl = "DetailUrl"
            )
        )

        whenever(eventHandler.handledEvents).thenReturn(
            mapOf(
                SessionAppointmentSubmitted to {
                    EventProcessingResult.Success(SessionAppointmentSubmitted, mapOf("property" to "value"))
                }
            )
        )
        handler = ReferAndMonitorHandler(converter, telemetryService, listOf(eventHandler))

        handler.handle(notification)

        verify(telemetryService).trackEvent(
            eq("SessionAppointmentSubmitted"),
            eq(mapOf("property" to "value")),
            any()
        )
    }

    @Test
    fun `failures are recorded in telemetry`() {
        val notification = Notification(
            HmppsDomainEvent(
                SessionAppointmentSubmitted.name,
                1,
                occurredAt = ZonedDateTime.now(),
                detailUrl = "DetailUrl"
            ),
            MessageAttributes(SessionAppointmentSubmitted.name)
        )

        whenever(eventHandler.handledEvents).thenReturn(
            mapOf(
                SessionAppointmentSubmitted to {
                    EventProcessingResult.Failure(IllegalStateException("Unable to process event '${SessionAppointmentSubmitted.name}'"))
                }
            )
        )
        handler = ReferAndMonitorHandler(converter, telemetryService, listOf(eventHandler))

        val ex = assertThrows<IllegalStateException> { handler.handle(notification) }
        val expectedMessage = "Unable to process event 'intervention.session-appointment.session-feedback-submitted'"
        assertThat(ex.message, equalTo(expectedMessage))

        verify(telemetryService).trackEvent(
            eq("IllegalStateException"),
            eq(mapOf("message" to expectedMessage)),
            any()
        )
    }
}

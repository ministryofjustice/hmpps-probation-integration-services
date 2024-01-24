package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.prepEvent
import uk.gov.justice.digital.hmpps.service.ApprovedPremisesService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var approvedPremisesService: ApprovedPremisesService

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `throws when no detail url is provided`() {
        val event = HmppsDomainEvent(eventType = "test", version = 1, occurredAt = ZonedDateTime.now())
        val exception = assertThrows<IllegalArgumentException> { event.url() }
        assertThat(exception.message, equalTo("Missing detail url"))
    }

    @Test
    fun `throws when no crn is provided`() {
        val event = HmppsDomainEvent(eventType = "test", version = 1, occurredAt = ZonedDateTime.now())
        val exception = assertThrows<IllegalArgumentException> { event.crn() }
        assertThat(exception.message, equalTo("Missing CRN"))
    }

    @Test
    fun `rejects unknown event types`() {
        val event = HmppsDomainEvent(eventType = "test", version = 1, occurredAt = ZonedDateTime.now(), personReference = PersonReference(
            listOf(PersonIdentifier("CRN", "X123456"))
        ))
        assertDoesNotThrow { handler.handle(Notification(event)) }
    }

    @Test
    fun `handles submitted applications`() {
        // Given a message
        val message = prepEvent("application-submitted")

        // When the message is received
        handler.handle(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(approvedPremisesService).applicationSubmitted(message.message)
        verify(telemetryService).trackEvent("ApplicationSubmitted", message.message.telemetryProperties())
    }

    @Test
    fun `handles assessed applications`() {
        // Given a message
        val message = prepEvent("application-assessed")

        // When the message is received
        handler.handle(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(approvedPremisesService).applicationAssessed(message.message)
        verify(telemetryService).trackEvent("ApplicationAssessed", message.message.telemetryProperties())
    }

    @Test
    fun `handles bookings made`() {
        // Given a message
        val message = prepEvent("booking-made")

        // When the message is received
        handler.handle(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(approvedPremisesService).bookingMade(message.message)
        verify(telemetryService).trackEvent("BookingMade", message.message.telemetryProperties())
    }

    @Test
    fun `handles person not arrived`() {
        // Given a message
        val message = prepEvent("person-not-arrived")

        // When the message is received
        handler.handle(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(approvedPremisesService).personNotArrived(message.message)
        verify(telemetryService).trackEvent("PersonNotArrived", message.message.telemetryProperties())
    }

    @Test
    fun `handles person arrived`() {
        // Given a message
        val message = prepEvent("person-arrived")

        // When the message is received
        handler.handle(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(approvedPremisesService).personArrived(message.message)
        verify(telemetryService).trackEvent("PersonArrived", message.message.telemetryProperties())
    }
}

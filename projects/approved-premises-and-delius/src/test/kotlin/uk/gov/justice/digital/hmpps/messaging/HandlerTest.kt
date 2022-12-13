package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.prepEvent
import uk.gov.justice.digital.hmpps.service.ApprovedPremisesService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock lateinit var telemetryService: TelemetryService
    @Mock lateinit var approvedPremisesService: ApprovedPremisesService
    @Mock lateinit var converter: NotificationConverter<HmppsDomainEvent>
    @InjectMocks lateinit var handler: Handler

    @Test
    fun `should reject unknown event types`() {
        // Given a message
        val message = prepEvent("application-submitted", 1234)

        // When the message is received
        val exception = assertThrows<IllegalArgumentException> {
            handler.handle(message.copy(message = message.message.copy(eventType = "UNKNOWN")))
        }

        // Then it is updated in Delius and logged to Telemetry
        assertThat(exception.message, equalTo("Unexpected event type UNKNOWN"))
    }

    @Test
    fun `should handle submitted applications`() {
        // Given a message
        val message = prepEvent("application-submitted", 1234)

        // When the message is received
        handler.handle(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(telemetryService).notificationReceived(message)
        verify(approvedPremisesService).applicationSubmitted(message.message)
        verify(telemetryService).trackEvent("ApplicationSubmitted", message.message.telemetryProperties())
    }
}

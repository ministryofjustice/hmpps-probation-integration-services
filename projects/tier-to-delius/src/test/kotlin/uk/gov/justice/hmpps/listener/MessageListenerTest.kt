package uk.gov.justice.hmpps.listener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.listener.MessageListener
import uk.gov.justice.digital.hmpps.message.HmppsEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class MessageListenerTest {
    @Mock lateinit var telemetryService: TelemetryService
    @InjectMocks lateinit var messageListener: MessageListener

    @Test
    fun messageIsLoggedToTelemetry() {
        // Given an event
        val event = HmppsEvent("test.event.type", 1, "https//detail/url", ZonedDateTime.now())

        // When it is received
        try { messageListener.receive(event) } catch (_: Throwable) { }

        // Then it is logged to telemetry
        verify(telemetryService).hmppsEventReceived(event)
    }
}

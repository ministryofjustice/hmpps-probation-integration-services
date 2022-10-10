package uk.gov.justice.hmpps.listener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.listener.MessageListener
import uk.gov.justice.digital.hmpps.listener.TierChangeEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class MessageListenerTest {
    @Mock lateinit var telemetryService: TelemetryService
    @InjectMocks lateinit var messageListener: MessageListener

    @Test
    fun messageIsLoggedToTelemetry() {
        // Given a message
        val message = Notification(message = TierChangeEvent("A123456", "test"))

        // When it is received
        try { messageListener.receive(message) } catch (_: Throwable) { }

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(message)
    }
}

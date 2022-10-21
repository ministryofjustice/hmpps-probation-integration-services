package uk.gov.justice.digital.hmpps.listener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class MessageListenerTest {
    @Mock lateinit var telemetryService: TelemetryService
    @InjectMocks lateinit var messageListener: MessageListener

    @Test
    fun `message is logged to telemetry`() {
        // Given a message
        val notification = Notification(message = MessageGenerator.EXAMPLE)

        // When it is received
        try {
            messageListener.receive(notification)
        } catch (_: NotImplementedError) {
            // Note: Remove this try/catch when the MessageListener logic has been implemented
        }

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
    }
}

package uk.gov.justice.hmpps.listener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.listener.MessageListener
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class MessageListenerTest {
    @Mock lateinit var telemetryService: TelemetryService
    @InjectMocks lateinit var messageListener: MessageListener

    @Test
    fun `message is logged to telemetry`() {
        // Given a message
        val notification = Notification(message = HmppsDomainEvent("test.event.type", 1, "https//detail/url", ZonedDateTime.now()))

        // When it is received
        try { messageListener.receive(notification) } catch (_: Throwable) { }

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
    }
}

package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock lateinit var telemetryService: TelemetryService

    @Mock lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @InjectMocks lateinit var handler: Handler

    @Test
    fun `message is logged to telemetry`() {
        // Given a message
        val notification = Notification(message = MessageGenerator.EXAMPLE)

        // When it is received
        try {
            handler.handle(notification)
        } catch (_: NotImplementedError) {
            // Note: Remove this try/catch when the Handler logic has been implemented
        }

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
    }
}

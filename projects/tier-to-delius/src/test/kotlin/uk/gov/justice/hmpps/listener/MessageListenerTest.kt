package uk.gov.justice.hmpps.listener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.integrations.tier.TierService
import uk.gov.justice.digital.hmpps.listener.MessageListener
import uk.gov.justice.digital.hmpps.listener.TierChangeEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class MessageListenerTest {
    @Mock lateinit var telemetryService: TelemetryService
    @Mock lateinit var tierService: TierService
    @InjectMocks lateinit var messageListener: MessageListener

    @Test
    fun `message is logged to telemetry`() {
        // Given a message
        val message = Notification(message = TierChangeEvent("A123456", "test"))

        // When it is received
        messageListener.receive(message)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(message)
    }

    @Test
    fun `should update tier`() {
        // Given a message
        val message = Notification(message = TierChangeEvent("A123456", "calculationId"))

        // When it is received
        messageListener.receive(message)

        // Then tier is updated
        verify(tierService).updateTier("A123456", "calculationId")
    }
}

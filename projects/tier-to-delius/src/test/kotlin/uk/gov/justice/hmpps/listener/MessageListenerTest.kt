package uk.gov.justice.hmpps.listener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculation
import uk.gov.justice.digital.hmpps.integrations.tier.TierClient
import uk.gov.justice.digital.hmpps.integrations.tier.TierService
import uk.gov.justice.digital.hmpps.listener.MessageListener
import uk.gov.justice.digital.hmpps.listener.TierChangeEvent
import uk.gov.justice.digital.hmpps.listener.telemetryProperties
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime.now

@ExtendWith(MockitoExtension::class)
internal class MessageListenerTest {
    @Mock lateinit var telemetryService: TelemetryService
    @Mock lateinit var tierClient: TierClient
    @Mock lateinit var tierService: TierService
    @InjectMocks lateinit var messageListener: MessageListener

    @Test
    fun `should update tier`() {
        // Given a message
        val message = Notification(message = TierChangeEvent("A123456", "calculationId"))
        // And a calculation
        val calculation = TierCalculation("someScore", "calculationId", now())
        whenever(tierClient.getTierCalculation("A123456", "calculationId")).thenReturn(calculation)

        // When the message is received
        messageListener.receive(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(telemetryService).notificationReceived(message)
        verify(tierClient).getTierCalculation("A123456", "calculationId")
        verify(tierService).updateTier("A123456", calculation)
        verify(telemetryService).trackEvent("TierUpdateSuccess", calculation.telemetryProperties("A123456"))
    }
}

package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculation
import uk.gov.justice.digital.hmpps.integrations.tier.TierClient
import uk.gov.justice.digital.hmpps.integrations.tier.TierService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.prepMessage
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI
import java.time.ZonedDateTime.now

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var tierClient: TierClient

    @Mock
    lateinit var tierService: TierService

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `should update tier`() {
        // Given a message
        val message = prepMessage("tier-calculation", 1234)
        // And a calculation
        val calculation = TierCalculation("someScore", "calculationId", now())
        whenever(tierClient.getTierCalculation(URI.create("http://localhost:1234/hmpps-tier/crn/A000001/tier/123e4567-e89b-12d3-a456-426614174000"))).thenReturn(
            calculation
        )

        // When the message is received
        handler.handle(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(telemetryService).notificationReceived(message)
        verify(tierClient).getTierCalculation(URI.create("http://localhost:1234/hmpps-tier/crn/A000001/tier/123e4567-e89b-12d3-a456-426614174000"))
        verify(tierService).updateTier("A000001", calculation)
        verify(telemetryService).trackEvent("TierUpdateSuccess", calculation.telemetryProperties("A000001"))
    }
}

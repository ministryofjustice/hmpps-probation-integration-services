package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.client.TierClient
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.prepEvent
import uk.gov.justice.digital.hmpps.service.TierUpdateService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime.now

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var detailService: DomainEventDetailService

    @Mock
    lateinit var tierUpdateService: TierUpdateService

    @Mock
    lateinit var tierClient: TierClient

    @Mock
    lateinit var featureFlags: FeatureFlags

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `should update tier`() {
        // Given a message
        val message = prepEvent("tier-calculation")
        // And a calculation
        val calculation = TierCalculation("someScore", "calculationId", now())
        whenever(tierClient.tierV3(any(), any())).thenReturn(calculation)
        // And the feature flag is enabled
        whenever(featureFlags.enabled("tier-to-delius-v3")).thenReturn(true)

        // When the message is received
        handler.handle(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(telemetryService).notificationReceived(message)
        verify(tierClient).tierV3(anyOrNull(), anyOrNull())
        verify(tierUpdateService).updateTier("A000001", calculation)
        verify(telemetryService).trackEvent(eq("TierUpdateSuccess"), argThat { this["crn"] == "A000001" }, any())
    }
}

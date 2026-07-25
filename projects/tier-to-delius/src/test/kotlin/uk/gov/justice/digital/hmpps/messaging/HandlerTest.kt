package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.client.TierClient
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculationV2
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculationV3
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.prepEvent
import uk.gov.justice.digital.hmpps.service.TierUpdateService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime.now

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

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

    val message = prepEvent("tier-calculation")

    @Test
    fun `should update v2 tier`() {
        givenFeatureFlags(phase1Enabled = false, phase2Enabled = false)
        val (tier2Calculation, tier3Calculation) = givenTierCalculations()

        // When the message is received
        handler.handle(message)

        verify(tierUpdateService).updateTier("A000001", tier2Calculation)
        verify(tierUpdateService, never()).updateTier("A000001", tier3Calculation)
        verify(tierUpdateService, never()).updateV3TierColumn(any(), any())
        verify(telemetryService).trackEvent(
            eq("TierUpdateSuccess"),
            check { it["crn"] == "A000001" && it["tierV2"] == "D2" },
            any()
        )
        verify(telemetryService, never()).trackEvent(eq("TierV3UpdateSuccess"), any(), any())
    }

    @Test
    fun `should update v3 tier column and use v2 tier when phase 1 is enabled`() {
        givenFeatureFlags(phase1Enabled = true, phase2Enabled = false)
        val (tier2Calculation, tier3Calculation) = givenTierCalculations()

        handler.handle(message)

        verify(tierUpdateService).updateV3TierColumn("A000001", tier3Calculation)
        verify(tierUpdateService).updateTier("A000001", tier2Calculation)
        verify(tierUpdateService, never()).updateTier("A000001", tier3Calculation)
        verify(telemetryService).trackEvent(
            eq("TierUpdateSuccess"),
            check { it["crn"] == "A000001" && it["tierV2"] == "D2" },
            any()
        )
        verify(telemetryService).trackEvent(
            eq("TierV3UpdateSuccess"),
            check { it["crn"] == "A000001" && it["tierV3"] == "B" },
            any()
        )
    }

    @Test
    fun `should use v3 tier when phase 2 is enabled`() {
        givenFeatureFlags(phase1Enabled = false, phase2Enabled = true)
        val (tier2Calculation, tier3Calculation) = givenTierCalculations()

        handler.handle(message)

        verify(tierUpdateService, never()).updateV3TierColumn(any(), any())
        verify(tierUpdateService).updateTier("A000001", tier3Calculation)
        verify(tierUpdateService, never()).updateTier("A000001", tier2Calculation)
    }

    @Test
    fun `should update v3 tier column and use v3 tier when both phases are enabled`() {
        givenFeatureFlags(phase1Enabled = true, phase2Enabled = true)
        val (tier2Calculation, tier3Calculation) = givenTierCalculations()

        handler.handle(message)

        verify(tierUpdateService).updateV3TierColumn("A000001", tier3Calculation)
        verify(tierUpdateService).updateTier("A000001", tier3Calculation)
        verify(tierUpdateService, never()).updateTier("A000001", tier2Calculation)
    }

    private fun givenFeatureFlags(phase1Enabled: Boolean, phase2Enabled: Boolean) {
        whenever(featureFlags.enabled("tier-v3-delius-phase-1")).thenReturn(phase1Enabled)
        whenever(featureFlags.enabled("tier-v3-delius-phase-2")).thenReturn(phase2Enabled)
    }

    private fun givenTierCalculations(): Pair<TierCalculationV2, TierCalculationV3> {
        val tier2Calculation = TierCalculationV2("D2", "123e4567-e89b-12d3-a456-426614174000", now())
        val tier3Calculation = TierCalculationV3("B", false, "123e4567-e89b-12d3-a456-426614174000", now())
        whenever(tierClient.tierV2("A000001", "123e4567-e89b-12d3-a456-426614174000")).thenReturn(tier2Calculation)
        whenever(tierClient.tierV3("A000001", "123e4567-e89b-12d3-a456-426614174000")).thenReturn(tier3Calculation)
        return tier2Calculation to tier3Calculation
    }
}

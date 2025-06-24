package uk.gov.justice.digital.hmpps.flags

import io.flipt.client.FliptClient
import io.flipt.client.FliptException
import io.flipt.client.models.BooleanEvaluationResponse
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.flags.FeatureFlags.FeatureFlagException
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class FeatureFlagsTest {
    @Mock
    private lateinit var fliptClient: FliptClient


    @InjectMocks
    private lateinit var featureFlags: FeatureFlags

    @BeforeEach
    fun setup() {
//        whenever(fliptClient.evaluation()).thenReturn(evaluation)
    }

    @Test
    fun `returns true if feature flag is enabled`() {
        withFlag("feature-flag-1", true)
        assertTrue(featureFlags.enabled("feature-flag-1"))
    }

    @Test
    fun `returns false if feature flag is not enabled`() {
        withFlag("feature-flag-2", false)
        assertFalse(featureFlags.enabled("feature-flag-2"))
    }

    @Test
    fun `throws error if feature flag is not defined`() {
        whenever(fliptClient.evaluateBoolean(any(), any(), any())).thenThrow(
            FliptException.EvaluationException("Not Found"))
        assertThrows<FeatureFlagException> { featureFlags.enabled("feature-flag-3") }
    }

    private fun withFlag(key: String, enabled: Boolean) {
        whenever(fliptClient.evaluateBoolean(any(), any(), any())).thenReturn(flag(key, enabled))
    }

    private fun flag(key: String, enabled: Boolean) =
        BooleanEvaluationResponse
        .builder()
        .enabled(enabled)
        .flagKey(key)
        .reason("DEFAULT_EVALUATION_REASON")
        .requestDurationMillis(100F)
        .timestamp(LocalTime.now().toString())
        .build()
}

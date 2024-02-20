package uk.gov.justice.digital.hmpps.flags

import io.flipt.api.FliptClient
import io.flipt.api.error.Error
import io.flipt.api.evaluation.Evaluation
import io.flipt.api.evaluation.models.BooleanEvaluationResponse
import io.flipt.api.evaluation.models.EvaluationReason
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

    @Mock
    private lateinit var evaluation: Evaluation

    @InjectMocks
    private lateinit var featureFlags: FeatureFlags

    @BeforeEach
    fun setup() {
        whenever(fliptClient.evaluation()).thenReturn(evaluation)
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
        whenever(evaluation.evaluateBoolean(any())).thenThrow(RuntimeException(Error(404, "Not Found")))
        assertThrows<FeatureFlagException> { featureFlags.enabled("feature-flag-3") }
    }

    private fun withFlag(key: String, enabled: Boolean) {
        whenever(evaluation.evaluateBoolean(any())).thenReturn(flag(key, enabled))
    }

    private fun flag(key: String, enabled: Boolean) = BooleanEvaluationResponse(
        enabled,
        key,
        EvaluationReason.MATCH_EVALUATION_REASON,
        100F,
        LocalTime.now().toString()
    )
}

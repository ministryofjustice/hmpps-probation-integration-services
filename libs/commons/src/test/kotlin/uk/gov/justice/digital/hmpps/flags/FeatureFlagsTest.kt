package uk.gov.justice.digital.hmpps.flags

import com.flipt.api.FliptApiClient
import com.flipt.api.client.flags.FlagsClient
import com.flipt.api.client.flags.endpoints.Get
import com.flipt.api.client.flags.exceptions.GetException
import com.flipt.api.client.flags.types.Flag
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.flags.FeatureFlags.FeatureFlagException
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class FeatureFlagsTest {
    @Mock
    private lateinit var fliptApiClient: FliptApiClient

    @Mock
    private lateinit var flagsClient: FlagsClient

    @InjectMocks
    private lateinit var featureFlags: FeatureFlags

    @BeforeEach
    fun setup() {
        whenever(fliptApiClient.flags()).thenReturn(flagsClient)
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
        whenever(flagsClient.get(Get.Request.builder().key("feature-flag-3").build()))
            .thenThrow(GetException.other("Not found", 404))
        assertThrows<FeatureFlagException> { featureFlags.enabled("feature-flag-3") }
    }

    private fun withFlag(
        key: String,
        enabled: Boolean,
    ) {
        whenever(flagsClient.get(Get.Request.builder().key(key).build())).thenReturn(flag(key, enabled))
    }

    private fun flag(
        key: String,
        enabled: Boolean,
    ) =
        Flag.builder()
            .key(key)
            .name("Name")
            .description("Description")
            .enabled(enabled)
            .createdAt(ZonedDateTime.now().toString())
            .updatedAt(ZonedDateTime.now().toString())
            .build()
}

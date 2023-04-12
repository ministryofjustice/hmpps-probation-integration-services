package uk.gov.justice.digital.hmpps.flags

import com.flipt.api.FliptApiClient
import org.springframework.stereotype.Service

@Service
class FeatureFlags(
    private val client: FliptApiClient?
) {
    fun enabled(key: String) = if (client == null) {
        throw FeatureFlagConfigurationException()
    } else {
        try {
            client.flags().get("default", key).enabled
        } catch (e: Exception) {
            throw FeatureFlagException(key, e)
        }
    }

    class FeatureFlagConfigurationException(override val message: String = "Flipt client not configured. Make sure FLIPT_URL and FLIPT_TOKEN are set") : IllegalStateException()
    class FeatureFlagException(val key: String, e: Exception) : RuntimeException("Unable to retrieve '$key' flag", e)
}

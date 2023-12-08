package uk.gov.justice.digital.hmpps.flags

import com.flipt.api.FliptApiClient
import com.flipt.api.client.flags.endpoints.Get
import org.springframework.stereotype.Service

@Service
class FeatureFlags(
    private val client: FliptApiClient?,
) {
    fun enabled(key: String) =
        try {
            client == null || client.flags()[Get.Request.builder().key(key).build()].enabled
        } catch (e: Exception) {
            throw FeatureFlagException(key, e)
        }

    class FeatureFlagException(val key: String, e: Exception) : RuntimeException("Unable to retrieve '$key' flag", e)
}

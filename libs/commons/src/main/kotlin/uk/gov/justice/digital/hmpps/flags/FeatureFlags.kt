package uk.gov.justice.digital.hmpps.flags

import io.flipt.api.FliptClient
import io.flipt.api.evaluation.models.EvaluationRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FeatureFlags(
    private val client: FliptClient?
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun enabled(key: String) = try {
        if (client == null) {
            log.warn("Flipt client not configured, all feature flags enabled.")
            true
        } else {
            client.evaluation()
                .evaluateBoolean(EvaluationRequest.builder().namespaceKey("probation-integration").flagKey(key).build())
                .isEnabled
        }
    } catch (e: Exception) {
        throw FeatureFlagException(key, e)
    }

    class FeatureFlagException(val key: String, e: Exception) : RuntimeException("Unable to retrieve '$key' flag", e)
}

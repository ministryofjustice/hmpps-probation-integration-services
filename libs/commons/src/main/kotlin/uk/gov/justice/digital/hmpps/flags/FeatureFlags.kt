package uk.gov.justice.digital.hmpps.flags

import io.flipt.api.FliptClient
import io.flipt.api.evaluation.models.EvaluationRequest
import org.springframework.stereotype.Service

@Service
class FeatureFlags(
    private val client: FliptClient?
) {
    fun enabled(key: String) = try {
        client == null || client.evaluation()
            .evaluateBoolean(EvaluationRequest.builder().namespaceKey("probation-integration").flagKey(key).build())
            .isEnabled
    } catch (e: Exception) {
        throw FeatureFlagException(key, e)
    }

    class FeatureFlagException(val key: String, e: Exception) : RuntimeException("Unable to retrieve '$key' flag", e)
}

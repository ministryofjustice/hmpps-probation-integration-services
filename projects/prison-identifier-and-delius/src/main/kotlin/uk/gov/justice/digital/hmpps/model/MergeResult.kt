package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

sealed interface MergeResult {
    val reason: String
    val telemetryProperties: Map<String, Any?>

    fun name() = "MergeResult${this::class.simpleName}"

    data class Ignored(override val reason: String, override val telemetryProperties: Map<String, Any?> = mapOf()) :
        MergeResult

    data class Success(override val reason: String, override val telemetryProperties: Map<String, Any?> = mapOf()) :
        MergeResult
}

fun TelemetryService.logResult(result: MergeResult, dryRun: Boolean) {
    trackEvent(
        result.name(),
        mapOf("reason" to result.reason, "dryRun" to dryRun.toString())
            + result.telemetryProperties.filterValues { it != null }.mapValues { it.value.toString() }
    )
}
package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.Custody
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

sealed interface MatchResult {
    val reason: String
    val telemetryProperties: Map<String, Any?>

    fun name() = "MatchResult${this::class.simpleName}"

    data class Ignored(
        override val reason: String,
        override val telemetryProperties: Map<String, Any?> = mapOf()
    ) : MatchResult

    data class NoMatch(
        override val reason: String,
        override val telemetryProperties: Map<String, Any?> = mapOf()
    ) : MatchResult

    data class Success(
        val prisonIdentifiers: PrisonIdentifiers,
        val person: Person,
        val custody: Custody?,
        override val telemetryProperties: Map<String, Any?> = mapOf(),
        override val reason: String = "Matched CRN ${person.crn} to NOMS number ${prisonIdentifiers.prisonerNumber}${custody?.let { " and custody ${custody.id} to ${prisonIdentifiers.bookingNumber}" } ?: ""}",
    ) : MatchResult
}

fun TelemetryService.logResult(result: MatchResult, dryRun: Boolean) {
    trackEvent(
        result.name(),
        mapOf("reason" to result.reason, "dryRun" to dryRun.toString())
            + result.telemetryProperties.filterValues { it != null }.mapValues { it.value.toString() }
    )
}
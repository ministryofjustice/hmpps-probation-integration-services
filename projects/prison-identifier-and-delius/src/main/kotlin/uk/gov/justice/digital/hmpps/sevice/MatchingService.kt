package uk.gov.justice.digital.hmpps.sevice

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sevice.model.ComponentMatch
import uk.gov.justice.digital.hmpps.sevice.model.PersonMatch
import uk.gov.justice.digital.hmpps.sevice.model.PotentialMatch
import uk.gov.justice.digital.hmpps.sevice.model.withinDays
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MatchingService(
    private val personRepository: PersonRepository,
    private val matcher: Matcher,
    private val matchWriter: MatchWriter,
    private val telemetryService: TelemetryService
) {

    fun matchWithPrisonData(crns: List<String>, trialOnly: Boolean) {
        crns.ifEmpty { personRepository.findAllCrns() }.asSequence()
            .mapNotNull(::match)
            .forEach { match ->
                logToTelemetry(match, trialOnly)
                if (!trialOnly && match is CompletedMatch.Successful) {
                    matchWriter.update(match.personMatch, match.custody) {
                        telemetryService.trackEvent(
                            "PersistingException",
                            mapOf("crn" to match.personMatch.person.crn, "exception" to it.message!!)
                        )
                    }
                }
            }
    }

    private fun match(crn: String): CompletedMatch? = try {
        matcher.matchCrn(crn)?.let { personMatch ->
            val matchedPrisoner = personMatch.match
            val matchingSentence = personMatch.person.events.filter {
                it.disposal?.custody != null && matchedPrisoner?.sentenceStartDate?.withinDays(it.disposal.startDate) == true
            }
            return when {
                matchedPrisoner != null -> CompletedMatch.Successful(
                    personMatch,
                    matchingSentence.firstOrNull()?.disposal?.custody
                )

                else -> CompletedMatch.Unsuccessful(
                    personMatch,
                    personMatch.person.events.mapNotNull { it.disposal?.startDate }
                )
            }
        }
    } catch (e: Exception) {
        telemetryService.trackEvent("MatchingException", mapOf("crn" to crn, "exception" to (e.message ?: "Unknown")))
        null
    }

    private fun logToTelemetry(completedMatch: CompletedMatch, trialOnly: Boolean) {
        when (completedMatch) {
            is CompletedMatch.Successful -> telemetryService.trackEvent(
                "SuccessfulMatch",
                completedMatch.telemetry() + ("dryRun" to trialOnly.toString())
            )

            is CompletedMatch.Unsuccessful -> telemetryService.trackEvent(
                "UnsuccessfulMatch",
                completedMatch.telemetry() + ("dryRun" to trialOnly.toString())
            )
        }
    }
}

sealed interface CompletedMatch {
    val personMatch: PersonMatch

    data class Successful(override val personMatch: PersonMatch, val custody: Custody?) : CompletedMatch

    data class Unsuccessful(override val personMatch: PersonMatch, val sentenceDates: List<LocalDate>) :
        CompletedMatch
}

fun CompletedMatch.sharedTelemetry(): Map<String, String> = listOfNotNull(
    "crn" to personMatch.person.crn,
    personMatch.person.nomsNumber?.let { "existingNomsId" to it },
    "custodialEvents" to personMatch.person.events.count { it.disposal?.custody != null }.toString(),
    personMatch.match?.prisonerNumber?.let { "matchedNomsId" to it },
    personMatch.match?.bookingNumber?.let { "matchedBookingNumber" to it },
    personMatch.match?.sentenceStartDate?.let { "matchedSentenceDate" to it.format(DateTimeFormatter.ISO_DATE) }
).toMap()

fun CompletedMatch.Successful.telemetry(): Map<String, String> =
    sharedTelemetry() + listOfNotNull(custody?.disposal?.startDate?.let { "sentenceDate" to it.format(DateTimeFormatter.ISO_DATE) }).toMap()

fun CompletedMatch.Unsuccessful.telemetry(): Map<String, String> =
    sharedTelemetry() + ("sentenceDates" to sentenceDates.joinToString { it.format(DateTimeFormatter.ISO_DATE) }) +
        personMatch.potentialMatches.telemetry()

fun List<PotentialMatch>.telemetry(): Map<String, String> = associate { potential ->
    potential.prisoner.prisonerNumber to potential.matches
        .filter { it.type != ComponentMatch.MatchType.FULL }
        .joinToString(separator = ", ") { "${it.name()}:${it.type}" }
}
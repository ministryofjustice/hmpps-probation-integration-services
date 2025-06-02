package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.Match
import uk.gov.justice.digital.hmpps.api.model.MatchRequest
import uk.gov.justice.digital.hmpps.api.model.MatchResponse
import uk.gov.justice.digital.hmpps.integrations.probationsearch.ProbationSearchClient
import uk.gov.justice.digital.hmpps.service.MatchService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@RestController
@RequestMapping
class MatchController(
    private val matchService: MatchService,
    private val probationSearch: ProbationSearchClient,
    private val telemetry: TelemetryService
) {
    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @PostMapping("/probation-cases/match")
    fun getMatches(
        @RequestBody request: MatchRequest,
        @RequestParam(required = false, defaultValue = "true") useSearch: Boolean
    ): MatchResponse {
        val service = matchService.findMatches(request)
        val search = probationSearch.match(request)

        val matchDiffs = service.matches vs search.matches
        if (service.matchedBy != search.matchedBy || matchDiffs.isNotEmpty()) {
            telemetry.trackEvent(
                "MatchingDiffEvent", mapOf(
                    "searchMatchedBy" to search.matchedBy.name,
                    "serviceMatchedBy" to service.matchedBy.name,
                    "searchMatchCount" to search.matches.size.toString(),
                    "serviceMatchCount" to service.matches.size.toString(),
                    "missing" to matchDiffs.filter { it.service == null && it.search != null }.joinToString(",") {
                        it.search!!.offender.otherIds.crn
                    },
                    "additional" to matchDiffs.filter { it.service != null && it.search == null }.joinToString(",") {
                        it.service!!.offender.otherIds.crn
                    }
                )
            )
        }

        return if (useSearch) search else service
    }

    private infix fun List<Match>.vs(other: List<Match>): Set<MatchDiff> {
        val matches = associateBy { it.offender.otherIds.crn }
        val otherMatches = other.associateBy { it.offender.otherIds.crn }
        return (matches.keys + otherMatches.keys).toSet().mapNotNull {
            findDiffs(matches[it], otherMatches[it])
        }.toSet()
    }

    private fun findDiffs(m1: Match?, m2: Match?): MatchDiff? {
        if (m1 == m2) return null
        if (m1?.offender?.copy(offenderAliases = listOf()) == m2?.offender?.copy(offenderAliases = listOf()) &&
            m1?.offender?.offenderAliases?.sortedBy { it.id } == m2?.offender?.offenderAliases?.sortedBy { it.id }
        ) {
            return null
        }
        return MatchDiff(m1, m2)
    }
}

data class MatchDiff(val service: Match?, val search: Match?)
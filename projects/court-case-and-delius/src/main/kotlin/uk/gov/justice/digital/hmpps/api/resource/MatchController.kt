package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.MatchRequest
import uk.gov.justice.digital.hmpps.api.model.MatchResponse
import uk.gov.justice.digital.hmpps.service.MatchService

@RestController
@RequestMapping
class MatchController(private val matchService: MatchService) {
    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @PostMapping("/probation-cases/match")
    fun getMatches(@RequestBody request: MatchRequest): MatchResponse = matchService.findMatches(request)
}
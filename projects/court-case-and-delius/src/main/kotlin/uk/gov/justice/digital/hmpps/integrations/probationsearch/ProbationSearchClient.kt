package uk.gov.justice.digital.hmpps.integrations.probationsearch

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.api.model.MatchRequest
import uk.gov.justice.digital.hmpps.api.model.MatchResponse

interface ProbationSearchClient {
    @PostExchange("/match")
    fun match(@RequestBody request: MatchRequest): MatchResponse
}
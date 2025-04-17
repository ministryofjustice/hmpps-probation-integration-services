package uk.gov.justice.digital.hmpps.integration.probationsearch

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.model.SearchRequest

interface ProbationSearchClient {
    @PostExchange("/search")
    fun findAll(@RequestBody request: SearchRequest): List<OffenderDetail>
}
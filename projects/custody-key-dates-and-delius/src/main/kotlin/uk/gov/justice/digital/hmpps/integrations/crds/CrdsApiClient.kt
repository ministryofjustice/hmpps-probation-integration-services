package uk.gov.justice.digital.hmpps.integrations.crds

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface CrdsApiClient {
    @GetExchange("/operative-sentence-envelope/{prisonerId}")
    fun getOperativeSentenceEnvelope(
        @PathVariable prisonerId: String
    ): OperativeSentenceEnvelope
}
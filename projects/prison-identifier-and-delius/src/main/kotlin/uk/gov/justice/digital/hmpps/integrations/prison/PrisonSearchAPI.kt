package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.sevice.model.SearchRequest
import uk.gov.justice.digital.hmpps.sevice.model.SearchResponse

interface PrisonSearchAPI {
    @PostExchange(url = "/global-search")
    fun matchPerson(@RequestBody body: SearchRequest): SearchResponse
}

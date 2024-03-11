package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

interface PrisonSearchApi {
    @PostExchange(url = "/global-search")
    fun matchPerson(@RequestBody body: SearchRequest): SearchResponse
}

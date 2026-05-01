package uk.gov.justice.digital.hmpps.client

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.client.model.CanonicalAddress

interface PersonClient {
    @GetExchange("/person/probation-integration/{crn}/address/{id}")
    fun getAddress(@PathVariable crn: String, @PathVariable id: String): CanonicalAddress
}

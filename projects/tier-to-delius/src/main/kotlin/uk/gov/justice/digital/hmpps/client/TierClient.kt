package uk.gov.justice.digital.hmpps.client

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculationV2
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculationV3

interface TierClient {
    @GetExchange(value = "/v3/crn/{crn}/tier/{id}")
    fun tierV3(@PathVariable crn: String, @PathVariable id: String): TierCalculationV3

    @GetExchange(value = "/v2/crn/{crn}/tier/{id}")
    fun tierV2(@PathVariable crn: String, @PathVariable id: String): TierCalculationV2
}

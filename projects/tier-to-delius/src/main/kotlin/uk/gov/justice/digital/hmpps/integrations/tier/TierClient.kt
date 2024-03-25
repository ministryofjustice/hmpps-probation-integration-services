package uk.gov.justice.digital.hmpps.integrations.tier

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface TierClient {
    @GetExchange
    fun getTierCalculation(baseUrl: URI): TierCalculation
}

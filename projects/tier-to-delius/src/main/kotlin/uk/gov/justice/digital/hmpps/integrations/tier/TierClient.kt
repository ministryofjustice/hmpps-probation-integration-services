package uk.gov.justice.digital.hmpps.integrations.tier

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.config.FeignOAuth2Config

@FeignClient(
    name = "Tier",
    url = "\${integrations.tier.url}",
    configuration = [FeignOAuth2Config::class]
)
interface TierClient {
    @GetMapping(value = ["/crn/{crn}/tier/{calculationId}"])
    fun getTierCalculation(
        @PathVariable("crn") crn: String,
        @PathVariable("calculationId") calculationId: String
    ): TierCalculation
}

package uk.gov.justice.digital.hmpps.integrations.tier

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.justice.digital.hmpps.config.FeignOAuth2Config
import java.net.URI

@FeignClient(
    name = "Tier",
    url = "https://dummy-url/to/be/overridden",
    configuration = [FeignOAuth2Config::class]
)
fun interface TierClient {
    @GetMapping
    fun getTierCalculation(baseUrl: URI): TierCalculation
}

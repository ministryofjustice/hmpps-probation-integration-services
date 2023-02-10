package uk.gov.justice.digital.hmpps.integrations.arn

import feign.Response
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.justice.digital.hmpps.config.ArnFeignConfig
import java.net.URI

@FeignClient(
    name = "arn-service",
    url = "https://dummy-url/to/be/overridden",
    configuration = [ArnFeignConfig::class]
)
interface ArnClient {
    @GetMapping
    fun getUPWAssessment(baseUrl: URI): Response
}

package uk.gov.justice.digital.hmpps.integrations.psr

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.justice.digital.hmpps.config.PsrFeignConfig
import java.net.URI

@FeignClient(
    name = "pre-sentence-service",
    url = "https://dummy-url/to/be/overridden",
    configuration = [PsrFeignConfig::class]
)
fun interface PsrClient {
    @GetMapping
    fun getPsrReport(baseUrl: URI): ByteArray
}

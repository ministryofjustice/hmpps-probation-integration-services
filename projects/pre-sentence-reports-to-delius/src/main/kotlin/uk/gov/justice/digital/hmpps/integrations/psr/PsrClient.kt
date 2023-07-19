package uk.gov.justice.digital.hmpps.integrations.psr

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import uk.gov.justice.digital.hmpps.config.PsrFeignConfig
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PsrReference
import java.net.URI

@FeignClient(
    name = "pre-sentence-service",
    url = "https://dummy-url/to/be/overridden",
    configuration = [PsrFeignConfig::class]
)
interface PsrClient {
    @GetMapping
    fun getPsrReport(baseUrl: URI): ByteArray

    @PostMapping
    fun createReport(uri: URI, body: Map<String, String>): PsrReference
}

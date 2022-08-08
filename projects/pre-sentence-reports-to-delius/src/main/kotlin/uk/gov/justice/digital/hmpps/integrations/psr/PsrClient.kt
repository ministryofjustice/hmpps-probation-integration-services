package uk.gov.justice.digital.hmpps.integrations.psr

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@FeignClient(name = "pre-sentence-reports", url = "https://dummy-url/to/be/overridden")
interface PsrClient {
    @GetMapping
    fun getPsrReport(baseUrl: URI): ByteArray
}

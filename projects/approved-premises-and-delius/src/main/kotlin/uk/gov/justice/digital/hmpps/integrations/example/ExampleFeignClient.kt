package uk.gov.justice.digital.hmpps.integrations.example

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.config.FeignOAuth2Config

@FeignClient(
    name = "example",
    url = "\${integrations.example.url}",
    configuration = [FeignOAuth2Config::class]
)
interface ExampleFeignClient {
    @GetMapping(value = ["/example/{inputId}"])
    fun getExampleAPICall(@PathVariable("inputId") inputId: String): String
}

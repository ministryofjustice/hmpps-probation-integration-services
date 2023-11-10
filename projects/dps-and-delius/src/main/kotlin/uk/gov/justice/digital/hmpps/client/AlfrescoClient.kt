package uk.gov.justice.digital.hmpps.client

import feign.Response
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.config.FeignConfig

@FeignClient(name = "alfresco", url = "\${integrations.alfresco.url}", configuration = [FeignConfig::class])
interface AlfrescoClient {
    @GetMapping(value = ["/fetch/{id}"])
    fun getDocument(@PathVariable id: String): Response
}

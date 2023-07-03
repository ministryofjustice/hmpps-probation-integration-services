package uk.gov.justice.digital.hmpps.integrations.alfresco

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import uk.gov.justice.digital.hmpps.config.AlfrescoFeignConfig

@FeignClient(name = "alfresco", url = "\${integrations.alfresco.url}", configuration = [AlfrescoFeignConfig::class])
interface AlfrescoClient {
    @PostMapping(value = ["/uploadnew"])
    fun addDocument(@RequestBody body: MultiValueMap<String, HttpEntity<*>>): AlfrescoDocument
}

package uk.gov.justice.digital.hmpps.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.config.FeignConfig

@FeignClient(name = "alfresco", url = "\${integrations.alfresco.url}", configuration = [FeignConfig::class])
interface AlfrescoClient {
    @GetMapping(value = ["/fetch/{id}"])
    fun getDocument(@PathVariable id: String): ResponseEntity<Resource>
}

package uk.gov.justice.digital.hmpps.integrations.alfresco

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import uk.gov.justice.digital.hmpps.config.AlfrescoFeignConfig

@FeignClient(name = "alfresco", url = "\${integrations.alfresco.url}", configuration = [AlfrescoFeignConfig::class])
interface AlfrescoClient {
    @PutMapping(value = ["/release/{id}"])
    fun releaseDocument(@PathVariable id: String)

    @PostMapping(value = ["/uploadandrelease/{id}"])
    fun updateDocument(@PathVariable id: String, @RequestBody body: MultiValueMap<String, HttpEntity<*>>)

    @PostMapping(value = ["/uploadnew"])
    fun uploadNewDocument(@RequestBody body: MultiValueMap<String, HttpEntity<*>>): AlfrescoUploadResponse
}

data class AlfrescoUploadResponse(@JsonAlias("ID") val id: String)

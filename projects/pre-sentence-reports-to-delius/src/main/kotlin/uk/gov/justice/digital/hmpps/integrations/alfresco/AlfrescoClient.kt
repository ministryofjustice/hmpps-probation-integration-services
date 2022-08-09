package uk.gov.justice.digital.hmpps.integrations.alfresco

import feign.Headers
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import uk.gov.justice.digital.hmpps.config.AlfrescoFeignConfig

@FeignClient(name = "alfresco",url = "\${integrations.alfresco.url}", configuration = [AlfrescoFeignConfig::class])
interface AlfrescoClient {
    @PutMapping(value = ["/release/{id}"])
    fun releaseDocument(@PathVariable id: String)

    @PutMapping(value = ["/uploadandrelease/{id}"])
    @Headers("Content-Type: multipart/form-data")
    fun updateDocument(@PathVariable id: String, @RequestBody body: MultipartBodyBuilder)
}

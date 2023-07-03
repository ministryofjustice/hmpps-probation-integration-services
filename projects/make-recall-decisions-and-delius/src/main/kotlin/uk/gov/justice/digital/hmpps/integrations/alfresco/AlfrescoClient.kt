package uk.gov.justice.digital.hmpps.integrations.alfresco

import feign.RequestInterceptor
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.security.ServiceContext

@FeignClient(name = "alfresco", url = "\${integrations.alfresco.url}", configuration = [AlfrescoFeignConfig::class])
interface AlfrescoClient {
    @GetMapping(value = ["/fetch/{id}"])
    fun getDocument(@PathVariable id: String): ResponseEntity<Resource>
}

class AlfrescoFeignConfig {
    @Bean
    fun requestInterceptor() = RequestInterceptor { template ->
        template.header("X-DocRepository-Remote-User", "N00")
        template.header("X-DocRepository-Real-Remote-User", ServiceContext.servicePrincipal()!!.username)
        template.header("Content-Type: multipart/form-data")
    }
}

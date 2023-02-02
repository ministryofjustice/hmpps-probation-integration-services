package uk.gov.justice.digital.hmpps.config

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import uk.gov.justice.digital.hmpps.security.ServiceContext

class AlfrescoFeignConfig {
    @Bean
    fun requestInterceptor() = RequestInterceptor { template ->
        template.header("X-DocRepository-Remote-User", "N00")
        template.header("X-DocRepository-Real-Remote-User", ServiceContext.servicePrincipal()!!.username)
        template.header("Content-Type: multipart/form-data")
    }
}

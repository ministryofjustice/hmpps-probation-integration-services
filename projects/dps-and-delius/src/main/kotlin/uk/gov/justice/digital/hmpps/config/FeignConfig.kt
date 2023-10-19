package uk.gov.justice.digital.hmpps.config

import feign.RequestInterceptor
import feign.Retryer
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.client.AlfrescoClient
import uk.gov.justice.digital.hmpps.security.ServiceContext

@Configuration
@EnableFeignClients(clients = [AlfrescoClient::class])
class FeignConfig {
    @Bean
    fun retryer() = Retryer.Default()

    @Bean
    fun requestInterceptor() = RequestInterceptor { template ->
        template.header("X-DocRepository-Remote-User", "N00")
        template.header("X-DocRepository-Real-Remote-User", ServiceContext.servicePrincipal()!!.username)
        template.header("Content-Type: multipart/form-data")
    }
}

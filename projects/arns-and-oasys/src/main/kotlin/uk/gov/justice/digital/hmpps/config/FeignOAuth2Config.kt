package uk.gov.justice.digital.hmpps.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import uk.gov.justice.digital.hmpps.config.feign.FeignConfig

@Configuration
@EnableFeignClients(clients = [])
class FeignOAuth2Config(
    authorizedClientManager: OAuth2AuthorizedClientManager
) : FeignConfig(authorizedClientManager) {
    override fun registrationId() = "arns-and-oasys"
}

package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import uk.gov.justice.digital.hmpps.config.feign.FeignConfig

// @EnableFeignClients(clients = [ExampleFeignClient::class])
@Configuration
class FeignOAuth2Config(
    authorizedClientManager: OAuth2AuthorizedClientManager
) : FeignConfig(authorizedClientManager) {
    override fun registrationId() = "unpaid-work-and-delius"
}

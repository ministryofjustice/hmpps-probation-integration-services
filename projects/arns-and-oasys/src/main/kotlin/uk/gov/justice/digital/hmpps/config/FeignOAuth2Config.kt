package uk.gov.justice.digital.hmpps.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import uk.gov.justice.digital.hmpps.config.feign.FeignConfig
import uk.gov.justice.digital.hmpps.integrations.oasys.OrdsClient

@Configuration
@EnableFeignClients(clients = [OrdsClient::class])
class FeignOAuth2Config(
    authorizedClientManager: OAuth2AuthorizedClientManager
) : FeignConfig(authorizedClientManager) {
    override fun registrationId() = "arns-and-oasys"
}

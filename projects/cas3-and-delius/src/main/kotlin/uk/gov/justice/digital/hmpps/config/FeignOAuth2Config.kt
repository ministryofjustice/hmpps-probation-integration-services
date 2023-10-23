package uk.gov.justice.digital.hmpps.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import uk.gov.justice.digital.hmpps.config.feign.FeignConfig
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.ApprovedPremisesApiClient

@Configuration
@EnableFeignClients(clients = [ApprovedPremisesApiClient::class])
class FeignOAuth2Config(
    authorizedClientManager: OAuth2AuthorizedClientManager
) : FeignConfig(authorizedClientManager) {
    override fun registrationId() = "cas3-and-delius"
}

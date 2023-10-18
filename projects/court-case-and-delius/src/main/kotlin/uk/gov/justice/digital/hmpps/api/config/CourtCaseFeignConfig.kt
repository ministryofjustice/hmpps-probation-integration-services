package uk.gov.justice.digital.hmpps.api.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import uk.gov.justice.digital.hmpps.config.feign.FeignConfig
import uk.gov.justice.digital.hmpps.integrations.courtcase.CourtCaseClient

@Configuration
@EnableFeignClients(clients = [CourtCaseClient::class])
class CourtCaseFeignConfig(
    authorizedClientManager: OAuth2AuthorizedClientManager
) : FeignConfig(authorizedClientManager) {
    override fun registrationId() = "court-case-and-delius"
}

package uk.gov.justice.digital.hmpps.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import uk.gov.justice.digital.hmpps.config.feign.FeignConfig
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.makerecalldecisions.MakeRecallDecisionsClient

@Configuration
@EnableFeignClients(clients = [AlfrescoClient::class, MakeRecallDecisionsClient::class])
class FeignConfig

class OAuth2FeignConfig(authorizedClientManager: OAuth2AuthorizedClientManager) : FeignConfig(authorizedClientManager) {
    override fun registrationId(): String = "make-recall-decisions-and-delius"
}

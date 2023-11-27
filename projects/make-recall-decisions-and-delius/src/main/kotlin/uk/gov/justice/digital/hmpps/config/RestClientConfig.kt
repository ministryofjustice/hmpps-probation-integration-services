package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.config.security.HmppsAuthInterceptor
import uk.gov.justice.digital.hmpps.integrations.makerecalldecisions.MakeRecallDecisionsClient

@Configuration
class RestClientConfig(private val clientManager: OAuth2AuthorizedClientManager) {

    @Bean
    fun makeRecallDecisionClient(
        restClientBuilder: RestClient.Builder
    ): MakeRecallDecisionsClient {
        val exchange = RestClientAdapter.create(
            restClientBuilder
                .requestInterceptor(HmppsAuthInterceptor(clientManager, "make-recall-decisions-and-delius"))
                .build()
        )
        return HttpServiceProxyFactory.builderFor(exchange).build()
            .createClient(MakeRecallDecisionsClient::class.java)
    }
}

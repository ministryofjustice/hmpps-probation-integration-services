package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.config.security.HmppsAuthInterceptor
import uk.gov.justice.digital.hmpps.integrations.cvl.CvlClient

@Configuration
class RestClientConfig(private val clientManager: OAuth2AuthorizedClientManager) {

    @Bean
    fun courtCaseClient(restClientBuilder: RestClient.Builder): CvlClient {
        val exchange = RestClientAdapter.create(
            restClientBuilder
                .requestInterceptor(HmppsAuthInterceptor(clientManager, "create-and-vary-a-licence-and-delius"))
                .build()
        )
        return HttpServiceProxyFactory.builderFor(exchange).build()
            .createClient(CvlClient::class.java)
    }
}

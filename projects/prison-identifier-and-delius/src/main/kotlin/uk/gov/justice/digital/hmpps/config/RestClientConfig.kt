package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.config.security.HmppsAuthInterceptor
import uk.gov.justice.digital.hmpps.integrations.example.ExampleClient

@Configuration
class RestClientConfig(private val clientManager: OAuth2AuthorizedClientManager) {

    @Bean
    fun prisonApiClient(
        restClientBuilder: RestClient.Builder,
        @Value("\${integrations.prison-api.url}") prisonApiBaseUrl: String
    ): ExampleClient {
        val exchange = RestClientAdapter.create(
            restClientBuilder
                .baseUrl("$prisonApiBaseUrl/api/bookings")
                .requestInterceptor(HmppsAuthInterceptor(clientManager, "prison-identifier-and-delius"))
                .build()
        )
        return HttpServiceProxyFactory.builderFor(exchange).build()
            .createClient(ExampleClient::class.java)
    }
}

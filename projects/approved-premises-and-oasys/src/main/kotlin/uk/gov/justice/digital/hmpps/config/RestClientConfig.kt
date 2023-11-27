package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.config.security.HmppsAuthInterceptor
import uk.gov.justice.digital.hmpps.integrations.oasys.client.OasysClient

@Configuration
class RestClientConfig(private val clientManager: OAuth2AuthorizedClientManager) {

    @Bean
    fun oasysClient(
        restClientBuilder: RestClient.Builder,
        @Value("\${integrations.ords-oasys.url}") oasysBaseUrl: String
    ): OasysClient {
        val exchange = RestClientAdapter.create(
            restClientBuilder
                .baseUrl(oasysBaseUrl)
                .requestInterceptor(HmppsAuthInterceptor(clientManager, "ords-oasys"))
                .build()
        )
        return HttpServiceProxyFactory.builderFor(exchange).build()
            .createClient(OasysClient::class.java)
    }
}

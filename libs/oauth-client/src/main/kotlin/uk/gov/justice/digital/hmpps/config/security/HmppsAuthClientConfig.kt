package uk.gov.justice.digital.hmpps.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.Builder
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class HmppsAuthClientConfig(
    private val restClientBuilder: Builder,
    private val clientManager: OAuth2AuthorizedClientManager
) {
    @Bean
    fun hmppsAuthClient(): RestClient =
        restClientBuilder
            .requestInterceptor(HmppsAuthInterceptor(clientManager, "default"))
            .build()
}

inline fun <reified T> createClient(client: RestClient): T {
    return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build()
        .createClient(T::class.java)
}

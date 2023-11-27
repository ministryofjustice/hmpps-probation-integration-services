package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.config.security.HmppsAuthInterceptor
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.integrations.psr.PsrClient

@Configuration
class RestClientConfig(private val clientManager: OAuth2AuthorizedClientManager) {

    @Bean
    fun psrClient(
        restClientBuilder: RestClient.Builder
    ): PsrClient {
        val exchange = RestClientAdapter.create(
            restClientBuilder
                .requestInterceptor(HmppsAuthInterceptor(clientManager, "pre-sentence-reports"))
                .build()
        )
        return HttpServiceProxyFactory.builderFor(exchange).build()
            .createClient(PsrClient::class.java)
    }

    @Bean
    fun alfrescoUploadClient(@Qualifier("alfrescoRestClient") restClient: RestClient): AlfrescoUploadClient {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build()
            .createClient(AlfrescoUploadClient::class.java)
    }
}

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
import uk.gov.justice.digital.hmpps.integrations.arn.ArnClient

@Configuration
class RestClientConfig(private val clientManager: OAuth2AuthorizedClientManager) {

    @Bean
    fun arnClient(
        restClientBuilder: RestClient.Builder
    ): ArnClient {
        val exchange = RestClientAdapter.create(
            restClientBuilder
                .requestInterceptor(HmppsAuthInterceptor(clientManager, "unpaid-work-and-delius"))
                .build()
        )
        return HttpServiceProxyFactory.builderFor(exchange).build()
            .createClient(ArnClient::class.java)
    }

    @Bean
    fun alfrescoUploadClient(@Qualifier("alfrescoRestClient") alfrescoRestClient: RestClient): AlfrescoUploadClient {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(alfrescoRestClient)).build()
            .createClient(AlfrescoUploadClient::class.java)
    }
}

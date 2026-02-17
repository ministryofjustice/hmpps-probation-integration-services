package uk.gov.justice.digital.hmpps.config.http

import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.ValidatingConnectionPool
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.client.RestClientUtils.jettyRequestFactory
import uk.gov.justice.digital.hmpps.config.http.interceptor.AlfrescoInterceptor
import kotlin.time.Duration.Companion.seconds

@Configuration
class AlfrescoClientConfig(@Value("\${integrations.alfresco.url}") private val alfrescoBaseUrl: String) {
    @Bean
    fun alfrescoRestClient(): RestClient = RestClient.builder()
        .requestFactory(
            jettyRequestFactory(
                readTimeout = 60.seconds,
                client = HttpClient().also {
                    it.httpClientTransport.connectionPoolFactory = { destination ->
                        ValidatingConnectionPool(destination, it.maxConnectionsPerDestination, it.scheduler, 1000)
                    }
                })
        )
        .requestInterceptor(AlfrescoInterceptor())
        .baseUrl(alfrescoBaseUrl)
        .build()
}
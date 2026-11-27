package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.client.RestClientUtils.createClient
import uk.gov.justice.digital.hmpps.integration.AlfrescoUploadClient

@Configuration
class RestClientConfig(
    @Value("\${integrations.alfresco.url}") private val alfrescoBaseUrl: String,
    private val restClientBuilder: RestClient.Builder,
) {
    @Bean
    @Qualifier("alfrescoRestClient")
    fun alfrescoRestClient(): RestClient = restClientBuilder
        .baseUrl(alfrescoBaseUrl)
        .build()

    @Bean
    fun alfrescoUploadClient(
        @Qualifier("alfrescoRestClient") alfrescoRestClient: RestClient,
    ) = createClient<AlfrescoUploadClient>(alfrescoRestClient)
}

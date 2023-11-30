package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.config.security.createClient
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.integrations.psr.PsrClient

@Configuration
class RestClientConfig(private val hmppsAuthClient: RestClient) {

    @Bean
    fun psrClient() = createClient<PsrClient>(hmppsAuthClient)

    @Bean
    fun alfrescoUploadClient(alfrescoRestClient: RestClient) = createClient<AlfrescoUploadClient>(alfrescoRestClient)
}

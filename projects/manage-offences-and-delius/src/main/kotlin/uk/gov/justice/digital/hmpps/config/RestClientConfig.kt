package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.client.ManageOffencesClient
import uk.gov.justice.digital.hmpps.config.security.createClient

@Configuration
class RestClientConfig(private val hmppsAuthClient: RestClient) {

    @Bean
    fun manageOffencesClient(@Value("\${integrations.manage-offences.url}") moBaseUrl: String) =
        createClient<ManageOffencesClient>(hmppsAuthClient.mutate().baseUrl(moBaseUrl).build())
}

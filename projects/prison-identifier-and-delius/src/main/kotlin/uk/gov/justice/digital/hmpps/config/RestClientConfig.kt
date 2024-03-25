package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.client.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.client.ProbationSearchClient
import uk.gov.justice.digital.hmpps.config.security.createClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {

    @Bean
    fun prisonerSearchClient(@Value("\${integrations.prisoner-search.url}") apiBaseUrl: String): PrisonerSearchClient =
        createClient(oauth2Client.mutate().baseUrl(apiBaseUrl).build())

    @Bean
    fun prisonApiClient(@Value("\${integrations.prison-api.url}") apiBaseUrl: String): PrisonApiClient =
        createClient(oauth2Client.mutate().baseUrl(apiBaseUrl).build())

    @Bean
    fun probationSearchClient(@Value("\${integrations.probation-search.url}") apiBaseUrl: String): ProbationSearchClient =
        createClient(oauth2Client.mutate().baseUrl(apiBaseUrl).build())
}

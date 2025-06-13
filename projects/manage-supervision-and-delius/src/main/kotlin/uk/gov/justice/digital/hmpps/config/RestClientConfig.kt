package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.client.BankHolidayClient
import uk.gov.justice.digital.hmpps.client.ProbationSearchClient
import uk.gov.justice.digital.hmpps.config.security.createClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient, private val restClientBuilder: RestClient.Builder) {

    @Bean
    fun probationSearchClient(@Value("\${integrations.probation-search.url}") apiBaseUrl: String): ProbationSearchClient =
        createClient(oauth2Client.mutate().baseUrl(apiBaseUrl).build())

    @Bean
    fun govukClient(@Value("\${gov.uk.url}") apiBaseUrl: String): BankHolidayClient =
        createClient(restClientBuilder.build().mutate().baseUrl(apiBaseUrl).build())
}

package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.config.security.createClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient

@Configuration
class RestClientConfig(private val hmppsAuthClient: RestClient) {

    @Bean
    fun courtCaseClient(@Value("\${integrations.prison-api.url}") prisonApiBaseUrl: String) =
        createClient<PrisonApiClient>(
            hmppsAuthClient.mutate()
                .baseUrl("$prisonApiBaseUrl/api/bookings")
                .build()
        )
}

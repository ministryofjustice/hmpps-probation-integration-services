package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.config.security.createClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {

    @Bean
    fun prisonApiClient(@Value("\${integrations.prison-api.url}") prisonApiBaseUrl: String) =
        createClient<PrisonApiClient>(
            oauth2Client.mutate()
                .baseUrl("$prisonApiBaseUrl/api")
                .build()
        )
}

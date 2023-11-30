package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.config.security.createClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient

@Configuration
class RestClientConfig(private val hmppsAuthClient: RestClient) {

    @Bean
    fun prisonCaseNotesClient() = createClient<PrisonCaseNotesClient>(hmppsAuthClient)
}

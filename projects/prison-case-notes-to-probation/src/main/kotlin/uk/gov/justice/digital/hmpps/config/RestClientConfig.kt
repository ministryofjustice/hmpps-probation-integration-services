package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.client.RestClientUtils.createClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonerAlertClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {

    @Bean
    fun prisonCaseNotesClient() = createClient<PrisonCaseNotesClient>(oauth2Client)

    @Bean
    fun prisonerAlertsClient() = createClient<PrisonerAlertClient>(oauth2Client)
}

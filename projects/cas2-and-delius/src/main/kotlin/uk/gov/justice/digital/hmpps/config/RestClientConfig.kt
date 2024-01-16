package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.client.approvedpremises.EventDetailsClient
import uk.gov.justice.digital.hmpps.config.security.createClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {
    @Bean
    fun eventDetailsClient() = createClient<EventDetailsClient>(oauth2Client)
}

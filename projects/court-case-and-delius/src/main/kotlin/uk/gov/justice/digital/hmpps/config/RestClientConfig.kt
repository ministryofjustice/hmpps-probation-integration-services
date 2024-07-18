package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.api.proxy.CommunityApiClient
import uk.gov.justice.digital.hmpps.config.security.createClient
import uk.gov.justice.digital.hmpps.integrations.courtcase.CourtCaseClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient, private val restClientBuilder: RestClient.Builder) {

    @Bean
    fun courtCaseClient() = createClient<CourtCaseClient>(oauth2Client)

    @Bean
    fun proxyClient() = restClientBuilder.build()

    @Bean
    fun communityApiClient() = createClient<CommunityApiClient>(proxyClient())
}

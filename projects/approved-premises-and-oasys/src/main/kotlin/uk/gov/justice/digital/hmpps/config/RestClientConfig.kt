package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.config.security.createClient
import uk.gov.justice.digital.hmpps.integrations.oasys.client.OasysClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {
    @Bean
    fun oasysClient(@Value("\${integrations.ords-oasys.url}") oasysBaseUrl: String) =
        createClient<OasysClient>(oauth2Client.mutate().baseUrl(oasysBaseUrl).build())
}

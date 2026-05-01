package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.client.PersonClient
import uk.gov.justice.digital.hmpps.client.RestClientUtils.createClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {
    @Bean
    fun personClient(@Value("\${integrations.core-person-record.url}") baseUrl: String) =
        createClient<PersonClient>(oauth2Client.mutate().baseUrl(baseUrl).build())
}

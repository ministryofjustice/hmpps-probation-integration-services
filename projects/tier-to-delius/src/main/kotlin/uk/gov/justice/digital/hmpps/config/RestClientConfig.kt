package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.client.RestClientUtils.createClient
import uk.gov.justice.digital.hmpps.client.TierClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {

    @Bean
    fun tierClient(@Value("\${integrations.tier.url}") url: String): TierClient = createClient(
        oauth2Client.mutate()
            .baseUrl(url)
            .build()
    )
}

package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.config.security.createClient
import uk.gov.justice.digital.hmpps.integrations.managepomcases.ManagePomCasesClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {

    @Bean
    fun managePomCasesClient() = createClient<ManagePomCasesClient>(oauth2Client)
}

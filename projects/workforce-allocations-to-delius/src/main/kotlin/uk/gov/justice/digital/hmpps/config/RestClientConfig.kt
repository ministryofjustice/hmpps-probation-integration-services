package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.config.security.createClient
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.WorkforceAllocationsClient

@Configuration
class RestClientConfig(private val hmppsAuthClient: RestClient) {

    @Bean
    fun workforceAllocationsClient() = createClient<WorkforceAllocationsClient>(hmppsAuthClient)
}

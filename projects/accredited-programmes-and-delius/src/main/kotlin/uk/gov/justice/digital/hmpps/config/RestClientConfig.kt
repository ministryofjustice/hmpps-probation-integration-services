package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {

}

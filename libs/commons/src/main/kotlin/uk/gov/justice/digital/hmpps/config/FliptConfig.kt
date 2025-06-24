package uk.gov.justice.digital.hmpps.config

import io.flipt.client.FliptClient
import io.flipt.client.models.ClientTokenAuthentication
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty("flipt.url")
class FliptConfig(
    @Value("\${flipt.url}") private val url: String,
    @Value("\${flipt.token}") private val token: String
) {
    @Bean
    fun fliptApiClient(): FliptClient =
        FliptClient.builder().namespace("probation-integration").url(url).authentication(ClientTokenAuthentication(token)).build()
}

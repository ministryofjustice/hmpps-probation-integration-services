package uk.gov.justice.digital.hmpps.config

import io.flipt.api.FliptClient
import io.flipt.api.authentication.ClientTokenAuthenticationStrategy
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
        FliptClient.builder().url(url).authentication(ClientTokenAuthenticationStrategy(token)).build()
}

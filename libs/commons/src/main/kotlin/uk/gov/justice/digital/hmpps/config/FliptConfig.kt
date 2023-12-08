package uk.gov.justice.digital.hmpps.config

import com.flipt.api.FliptApiClient
import com.flipt.api.core.BearerAuth
import com.flipt.api.core.Environment
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty("flipt.url")
class FliptConfig(
    @Value("\${flipt.url}") private val url: String,
    @Value("\${flipt.token}") private val token: String,
) {
    @Bean
    fun fliptApiClient() = FliptApiClient(Environment.custom(url), BearerAuth.of(token))
}

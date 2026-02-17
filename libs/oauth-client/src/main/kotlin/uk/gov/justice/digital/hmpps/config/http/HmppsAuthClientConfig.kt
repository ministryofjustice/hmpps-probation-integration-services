package uk.gov.justice.digital.hmpps.config.http

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.Builder
import uk.gov.justice.digital.hmpps.client.RestClientUtils
import uk.gov.justice.digital.hmpps.config.http.interceptor.HmppsAuthInterceptor
import uk.gov.justice.digital.hmpps.config.http.interceptor.RetryInterceptor

@Configuration
class HmppsAuthClientConfig(
    private val restClientBuilder: Builder,
    private val clientManager: OAuth2AuthorizedClientManager
) {
    @Bean
    fun oauth2Client(): RestClient = restClientBuilder
        .requestFactory(RestClientUtils.jettyRequestFactory())
        .requestInterceptor(HmppsAuthInterceptor(clientManager, "default"))
        .requestInterceptor(RetryInterceptor())
        .build()
}


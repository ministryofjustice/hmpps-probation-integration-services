package uk.gov.justice.digital.hmpps.config.security

import org.eclipse.jetty.client.HttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JettyClientHttpRequestFactory
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.Builder
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.time.Duration

@Configuration
class HmppsAuthClientConfig(
    private val restClientBuilder: Builder,
    private val clientManager: OAuth2AuthorizedClientManager
) {
    @Bean
    fun oauth2Client(): RestClient = restClientBuilder
        .requestFactory(JettyClientHttpRequestFactory(HttpClient().apply {
            responseBufferSize = 1024 * 1024 // 1 MB, to allow for larger buffered responses (e.g. OASys 401 page)
        }).apply {
            setConnectTimeout(Duration.ofSeconds(1))
            setReadTimeout(Duration.ofSeconds(5))
        })
        .requestInterceptor(HmppsAuthInterceptor(clientManager, "default"))
        .requestInterceptor(RetryInterceptor())
        .build()
}

inline fun <reified T> createClient(client: RestClient): T {
    return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build()
        .createClient(T::class.java)
}

fun <T> nullIfNotFound(fn: () -> T): T? = try {
    fn()
} catch (e: HttpClientErrorException.NotFound) {
    null
}
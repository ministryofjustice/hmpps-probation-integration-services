package uk.gov.justice.digital.hmpps.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.Builder
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class HmppsAuthClientConfig(
    private val restClientBuilder: Builder,
    private val clientManager: OAuth2AuthorizedClientManager
) {
    @Bean
    fun oauth2Client() = restClientBuilder
        .requestFactory(withTimeouts(Duration.ofSeconds(1), Duration.ofSeconds(5)))
        .requestInterceptor(HmppsAuthInterceptor(clientManager, "default"))
        .requestInterceptor(RetryInterceptor())
        .build()
}

fun withTimeouts(connection: Duration, read: Duration) =
    JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(connection).build())
        .also { it.setReadTimeout(read) }

inline fun <reified T> createClient(client: RestClient): T {
    return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build()
        .createClient(T::class.java)
}

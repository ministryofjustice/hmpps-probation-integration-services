package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.http.client.support.HttpRequestWrapper
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.config.security.RetryInterceptor
import uk.gov.justice.digital.hmpps.config.security.createClient
import uk.gov.justice.digital.hmpps.integrations.client.OsClient
import uk.gov.justice.digital.hmpps.integrations.client.ProbationSearchClient
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class RestClientConfig(private val oauth2Client: RestClient) {

    @Bean
    fun probationSearchClient(@Value("\${integrations.probation-search.url}") apiBaseUrl: String): ProbationSearchClient =
        createClient(oauth2Client.mutate().baseUrl(apiBaseUrl).build())

    @Bean
    fun osPlacesRestClient(
        @Value("\${os-places.api.key}") apiKey: String,
        @Value("\${os-places.api.url}") baseUrl: String
    ): OsClient = createClient(
        RestClient.builder()
            .requestFactory(withTimeouts(Duration.ofSeconds(1), Duration.ofSeconds(5)))
            .requestInterceptor(RetryInterceptor())
            .requestInterceptor { request, body, execution ->
                execution.execute(object : HttpRequestWrapper(request) {
                    override fun getURI() = request.uri
                        .let { UriComponentsBuilder.fromUri(it).queryParam("key", apiKey).build(true).toUri() }
                }, body)
            }
            .baseUrl(baseUrl)
            .build())

    fun withTimeouts(connection: Duration, read: Duration) =
        JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(connection).build())
            .also { it.setReadTimeout(read) }
}

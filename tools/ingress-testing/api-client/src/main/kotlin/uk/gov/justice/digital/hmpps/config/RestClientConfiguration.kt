package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class RestClientConfiguration(
    @Value("\${url:https://ingress-test.probation-integration.service.justice.gov.uk}") private val url: String,
    @Value("\${http1:false}") private val useHttp1: Boolean,
) {
    @Bean
    fun restClient(
        builder: RestClient.Builder
    ): RestClient {
        val httpClient = HttpClient.newBuilder()
            .version(if (useHttp1) HttpClient.Version.HTTP_1_1 else HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(1))
            .build()
        val requestFactory = JdkClientHttpRequestFactory(httpClient)
        requestFactory.setReadTimeout(Duration.ofSeconds(40))

        return builder
            .requestFactory(requestFactory)
            .baseUrl(url)
            .defaultHeaders {
                it.contentType = MediaType.APPLICATION_JSON
                it.accept = listOf(MediaType.APPLICATION_JSON)
            }
            .build()
    }
}

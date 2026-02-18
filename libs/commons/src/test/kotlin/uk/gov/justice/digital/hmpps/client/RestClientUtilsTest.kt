package uk.gov.justice.digital.hmpps.client

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.client.HttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.JettyClientHttpRequestFactory
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.service.annotation.GetExchange
import kotlin.time.Duration.Companion.seconds

class RestClientUtilsTest {

    interface TestClient {
        @GetExchange("/test")
        fun test(): String
    }

    @Test
    fun `creates type-safe rest client proxy`() {
        val restClient = RestClient.builder().baseUrl("http://localhost").build()
        val client = RestClientUtils.createClient<TestClient>(restClient)
        assertThat(client).isInstanceOf(TestClient::class.java)
    }

    @Test
    fun `creates jetty request factory with default timeouts`() {
        val factory = RestClientUtils.jettyRequestFactory()

        assertThat(factory.httpClient().connectTimeout).isEqualTo(1.seconds.inWholeMilliseconds)
        assertThat(factory.readTimeout()).isEqualTo(5.seconds.inWholeMilliseconds)
    }

    @Test
    fun `creates jetty request factory with provided client and timeouts`() {
        val client = HttpClient()
        val factory = RestClientUtils.jettyRequestFactory(2.seconds, 10.seconds, client)

        assertThat(factory.httpClient()).isSameAs(client)
        assertThat(factory.httpClient().connectTimeout).isEqualTo(2.seconds.inWholeMilliseconds)
        assertThat(factory.readTimeout()).isEqualTo(10.seconds.inWholeMilliseconds)
    }

    @Test
    fun `returns null when callback throws not found`() {
        val result: String? = RestClientUtils.nullIfNotFound {
            throw HttpClientErrorException.create(HttpStatus.NOT_FOUND, "not found", HttpHeaders(), null, null)
        }

        assertThat(result).isNull()
    }

    @Test
    fun `does not swallow other exceptions`() {
        assertThrows<IllegalStateException> {
            RestClientUtils.nullIfNotFound {
                throw IllegalStateException("boom")
            }
        }
    }

    private fun JettyClientHttpRequestFactory.httpClient() =
        JettyClientHttpRequestFactory::class.java.getDeclaredField("httpClient")
            .let { field ->
                field.isAccessible = true
                field.get(this) as HttpClient
            }

    private fun JettyClientHttpRequestFactory.readTimeout(): Long =
        JettyClientHttpRequestFactory::class.java.getDeclaredField("readTimeout")
            .let { field ->
                field.isAccessible = true
                field.getLong(this)
            }
}

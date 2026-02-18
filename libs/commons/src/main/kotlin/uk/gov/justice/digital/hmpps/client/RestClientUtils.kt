package uk.gov.justice.digital.hmpps.client

import org.eclipse.jetty.client.HttpClient
import org.springframework.http.client.JettyClientHttpRequestFactory
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object RestClientUtils {
    inline fun <reified T : Any> createClient(client: RestClient) =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build().createClient<T>()

    fun jettyRequestFactory(
        connectTimeout: Duration = 1.seconds,
        readTimeout: Duration = 5.seconds,
        client: HttpClient? = null
    ): JettyClientHttpRequestFactory {
        val factory = if (client != null) JettyClientHttpRequestFactory(client) else JettyClientHttpRequestFactory()
        factory.setConnectTimeout(connectTimeout.toJavaDuration())
        factory.setReadTimeout(readTimeout.toJavaDuration())
        return factory
    }

    fun <T> nullIfNotFound(fn: () -> T): T? = try {
        fn()
    } catch (_: HttpClientErrorException.NotFound) {
        null
    }
}
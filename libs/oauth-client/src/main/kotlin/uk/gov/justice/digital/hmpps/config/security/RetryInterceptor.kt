package uk.gov.justice.digital.hmpps.config.security

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import uk.gov.justice.digital.hmpps.retry.retry
import java.io.IOException
import java.time.Duration

class RetryInterceptor(private val retries: Int = 3, private val delay: Duration = Duration.ofMillis(200)) :
    ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse = retry(retries, listOf(IOException::class), delay) {
        execution.execute(request, body)
    }
}

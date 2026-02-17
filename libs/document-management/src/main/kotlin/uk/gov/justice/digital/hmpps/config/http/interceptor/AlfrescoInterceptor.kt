package uk.gov.justice.digital.hmpps.config.http.interceptor

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import uk.gov.justice.digital.hmpps.security.ServiceContext

class AlfrescoInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers["X-DocRepository-Remote-User"] = "N00"
        request.headers["X-DocRepository-Real-Remote-User"] = ServiceContext.servicePrincipal()?.username
        return execution.execute(request, body)
    }
}
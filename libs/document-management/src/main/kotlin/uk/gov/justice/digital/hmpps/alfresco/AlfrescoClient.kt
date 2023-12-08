package uk.gov.justice.digital.hmpps.alfresco

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.security.ServiceContext
import java.net.http.HttpClient
import java.time.Duration

@Component
@ConditionalOnProperty("integrations.alfresco.url")
class AlfrescoClient(
    @Qualifier("alfrescoRestClient") private val restClient: RestClient,
) {
    fun getDocumentById(id: String): RestClient.RequestHeadersSpec<*> =
        restClient.get().uri("/fetch/$id")
            .accept(MediaType.MULTIPART_FORM_DATA)

    fun streamDocument(
        id: String,
        filename: String,
    ): ResponseEntity<StreamingResponseBody> =
        getDocumentById(id).exchange({ _, res ->
            when (res.statusCode) {
                HttpStatus.OK ->
                    ResponseEntity.ok()
                        .headers {
                            it.copy(HttpHeaders.CONTENT_LENGTH, res)
                            it.copy(HttpHeaders.ETAG, res)
                            it.copy(HttpHeaders.LAST_MODIFIED, res)
                        }
                        .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(filename, Charsets.UTF_8).build().toString(),
                        )
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(StreamingResponseBody { output -> res.body.use { it.copyTo(output) } })

                HttpStatus.NOT_FOUND -> throw NotFoundException("Document content", "alfrescoId", id)

                else -> throw RuntimeException("Failed to download document. Alfresco responded with ${res.statusCode}.")
            }
        }, false)

    private fun HttpHeaders.copy(
        key: String,
        res: ConvertibleClientHttpResponse,
    ) {
        res.headers[key]?.also { this[key] = it }
    }
}

@Configuration
class AlfrescoClientConfig(
    @Value("\${integrations.alfresco.url}") private val alfrescoBaseUrl: String,
) {
    @Bean
    fun alfrescoRestClient() =
        RestClient.builder()
            .requestFactory(withTimeouts(Duration.ofSeconds(1), Duration.ofSeconds(30)))
            .requestInterceptor(AlfrescoInterceptor())
            .baseUrl(alfrescoBaseUrl)
            .build()
}

fun withTimeouts(
    connection: Duration,
    read: Duration,
) =
    JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(connection).build())
        .also { it.setReadTimeout(read) }

class AlfrescoInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        request.headers["X-DocRepository-Remote-User"] = "N00"
        request.headers["X-DocRepository-Real-Remote-User"] = ServiceContext.servicePrincipal()?.username
        return execution.execute(request, body)
    }
}

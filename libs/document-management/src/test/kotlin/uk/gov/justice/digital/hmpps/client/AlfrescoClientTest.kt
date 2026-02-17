package uk.gov.justice.digital.hmpps.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class AlfrescoClientTest {

    private val restClientBuilder = RestClient.builder().baseUrl("http://localhost")
    private val mockServer = MockRestServiceServer.bindTo(restClientBuilder).build()
    private val client = AlfrescoClient(restClientBuilder.build())

    @BeforeEach
    fun setUp() {
        mockServer.reset()
    }

    @Test
    fun `textSearch returns parsed response`() {
        val id = "folder-123"
        val query = "report"

        mockServer.expect(requestTo("http://localhost/search/text/$id?query=$query"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.ACCEPT, MediaType.MULTIPART_FORM_DATA_VALUE))
            .andRespond(
                withSuccess(
                    """
                    {
                      "numberOfDocuments": 1,
                      "maxResults": 10,
                      "startIndex": 0,
                      "documents": [{"id": "doc-1"}]
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        val response = client.textSearch(id, query)

        assertThat(response.numberOfDocuments).isEqualTo(1)
        assertThat(response.maxResults).isEqualTo(10)
        assertThat(response.documents.single().id).isEqualTo("doc-1")
        mockServer.verify()
    }

    @Test
    fun `textSearch throws when alfresco response status is not OK`() {
        val id = "folder-123"
        val query = "report"

        mockServer.expect(requestTo("http://localhost/search/text/$id?query=$query"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        assertThatThrownBy { client.textSearch(id, query) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Document text search failed. Alfresco responded with 500 INTERNAL_SERVER_ERROR.")

        mockServer.verify()
    }

    @Test
    fun `streamDocument returns response body and headers`() {
        val id = "00000000-0000-0000-0000-000000000001"
        val fileName = "test document.txt"
        val fileContents = "document-content"
        val responseHeaders = HttpHeaders().apply {
            contentLength = fileContents.toByteArray().size.toLong()
            eTag = "\"etag-value\""
            lastModified = 60000L
        }

        mockServer.expect(requestTo("http://localhost/fetch/$id"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.ACCEPT, MediaType.MULTIPART_FORM_DATA_VALUE))
            .andRespond(withSuccess(fileContents, MediaType.TEXT_PLAIN).headers(responseHeaders))

        val response = client.streamDocument(id, fileName)
        val output = ByteArrayOutputStream()

        response.body!!.writeTo(output)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType).isEqualTo(MediaType.TEXT_PLAIN)
        assertThat(response.headers.contentLength).isEqualTo(fileContents.toByteArray().size.toLong())
        assertThat(response.headers.eTag).isEqualTo("\"etag-value\"")
        assertThat(response.headers.lastModified).isEqualTo(60000L)
        assertThat(response.headers.contentDisposition.filename).isEqualTo(fileName)
        assertThat(output.toString(StandardCharsets.UTF_8)).isEqualTo(fileContents)
        mockServer.verify()
    }

    @Test
    fun `streamDocument throws not found when alfresco returns 404`() {
        val id = "00000000-0000-0000-0000-000000000002"

        mockServer.expect(requestTo("http://localhost/fetch/$id"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        assertThatThrownBy { client.streamDocument(id, "document.pdf") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Document content with alfrescoId of $id not found")

        mockServer.verify()
    }

    @Test
    fun `streamDocument validates id is a uuid`() {
        assertThatThrownBy { client.streamDocument("not-a-uuid", "document.pdf") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}

package uk.gov.justice.digital.hmpps.service

import feign.Util.CONTENT_LENGTH
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository

@ExtendWith(MockitoExtension::class)
internal class DocumentServiceTest {
    @Mock
    lateinit var alfrescoClient: AlfrescoClient

    @Mock
    lateinit var documentRepository: DocumentRepository

    @Mock
    lateinit var resource: Resource

    @InjectMocks
    lateinit var documentService: DocumentService

    @Test
    fun `document content is returned and headers are filtered`() {
        val alfrescoResponse = ResponseEntity.ok()
            .header(CONTENT_LENGTH, "123")
            .header("Other header", "other")
            .body(resource)
        whenever(alfrescoClient.getDocument("uuid")).thenReturn(alfrescoResponse)
        whenever(documentRepository.findNameByPersonCrnAndAlfrescoId("X000001", "uuid")).thenReturn("My filename.pdf")

        val document = documentService.downloadDocument("X000001", "uuid")

        assertThat(document.statusCode, equalTo(HttpStatus.OK))
        assertThat(document.headers.contentDisposition.filename, equalTo("My filename.pdf"))
        assertThat(document.headers.contentLength, equalTo(123))
        assertThat(document.headers, not(hasKey("Other header")))
        assertThat(document.body, equalTo(alfrescoResponse.body))
    }

    @Test
    fun `throws not found when document is missing or not linked to CRN`() {
        whenever(documentRepository.findNameByPersonCrnAndAlfrescoId("X000001", "uuid")).thenReturn(null)

        val exception = assertThrows<NotFoundException> { documentService.downloadDocument("X000001", "uuid") }

        assertThat(exception.message, equalTo("Document with id of uuid not found for CRN X000001"))
    }

    @Test
    fun `throws not found on alfresco 404`() {
        whenever(alfrescoClient.getDocument("uuid")).thenReturn(ResponseEntity.notFound().build())
        whenever(documentRepository.findNameByPersonCrnAndAlfrescoId("X000001", "uuid")).thenReturn("My filename.pdf")

        val exception = assertThrows<NotFoundException> { documentService.downloadDocument("X000001", "uuid") }

        assertThat(exception.message, equalTo("Document content with id of uuid not found for CRN X000001"))
    }

    @Test
    fun `alfresco error is handled`() {
        whenever(alfrescoClient.getDocument("uuid")).thenReturn(ResponseEntity.internalServerError().build())
        whenever(documentRepository.findNameByPersonCrnAndAlfrescoId("X000001", "uuid")).thenReturn("My filename.pdf")

        val exception = assertThrows<RuntimeException> { documentService.downloadDocument("X000001", "uuid") }

        assertThat(exception.message, equalTo("Failed to download document. Alfresco responded with 500 INTERNAL_SERVER_ERROR."))
    }
}

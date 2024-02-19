package uk.gov.justice.digital.hmpps.api.resource

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentService

@ExtendWith(MockitoExtension::class)
class DocumentResourceTest {

    @Mock
    private lateinit var documentService: DocumentService

    @InjectMocks
    private lateinit var resource: DocumentResource

    @Test
    fun `get documents no results`() {
        val crn = "D111111"
        whenever(documentService.getDocumentsByCrn(crn)).thenReturn(listOf())
        val res = resource.findDocuments(crn)
        assertTrue(res.isEmpty())
    }

    @Test
    fun `get document no results`() {
        val crn = "D111111"
        val id = "123-123"
        val expectedResponse = ResponseEntity<StreamingResponseBody>(HttpStatus.OK)
        whenever(documentService.getDocument(crn, id)).thenReturn(expectedResponse)
        val res = resource.getDocument(crn, id)
        assertEquals(expectedResponse.statusCode, res.statusCode)
    }
}

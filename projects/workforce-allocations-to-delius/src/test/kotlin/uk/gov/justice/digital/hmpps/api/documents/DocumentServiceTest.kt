package uk.gov.justice.digital.hmpps.api.documents

import org.junit.jupiter.api.Assertions.assertEquals
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
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocEvent
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocPerson
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.EventDocument
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.OffenderDocument
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

@ExtendWith(MockitoExtension::class)
class DocumentServiceTest {

    @Mock
    private lateinit var docPersonRepository: DocPersonRepository

    @Mock
    private lateinit var documentRepository: DocumentRepository

    @Mock
    private lateinit var alfrescoClient: AlfrescoClient

    @InjectMocks
    private lateinit var service: DocumentService

    @Test
    fun `get documents person not found`() {
        val crn = "D111111"
        whenever(docPersonRepository.findByCrn(crn)).thenReturn(null)
        val ex = assertThrows<NotFoundException> {
            service.getDocumentsByCrn(crn)
        }
        val expected = NotFoundException("Person", "crn", crn)
        assertEquals(expected.message, ex.message)
    }

    @Test
    fun `get documents`() {
        val crn = "D111111"
        whenever(docPersonRepository.findByCrn(crn)).thenReturn(DocPerson(1L, crn, false, null, null, null))
        whenever(documentRepository.findAllByPersonIdAndSoftDeletedIsFalse(1L)).thenReturn(listOf(OffenderDocument()))
        val documents = service.getDocumentsByCrn(crn)
        assertEquals(1, documents.size)
    }

    @Test
    fun `get event documents`() {
        val crn = "D111111"
        whenever(docPersonRepository.findByCrn(crn)).thenReturn(DocPerson(1L, crn, false, null, null, null))

        val eventDocuments = listOf(
            EventDocument(
                DocEvent(
                    1L,
                    Person(1L, crn, false),
                    true,
                    "1",
                    null,
                    null,
                    null,
                    null,
                    null,
                )
            ),
            EventDocument(
                DocEvent(
                    1L,
                    Person(1L, crn, false),
                    false,
                    "1",
                    null,
                    null,
                    null,
                    null,
                    null,
                )
            )
        )

        whenever(documentRepository.findAllByPersonIdAndSoftDeletedIsFalse(1L)).thenReturn(eventDocuments)
        val documents = service.getDocumentsByCrn(crn)
        assertEquals(2, documents.size)
    }

    @Test
    fun `get document`() {
        val crn = "D111111"
        val id = "123-123"
        val document = OffenderDocument()
        document.personId = 1L
        val expectedResponse = ResponseEntity<Resource>(HttpStatus.OK)

        whenever(docPersonRepository.findByCrn(crn)).thenReturn(DocPerson(1L, crn, false, null, null, null))
        whenever(documentRepository.findByAlfrescoIdAndSoftDeletedIsFalse(id)).thenReturn(document)
        whenever(alfrescoClient.getDocument(id)).thenReturn(expectedResponse)
        val response = service.getDocument(crn, id)
        assertEquals(expectedResponse.statusCode, response.statusCode)
    }

    @Test
    fun `get document person not found`() {
        val crn = "D111111"
        val id = "123-123"
        whenever(docPersonRepository.findByCrn(crn)).thenReturn(null)
        val ex = assertThrows<NotFoundException> {
            service.getDocument(crn, id)
        }
        val expected = NotFoundException("Person", "crn", crn)
        assertEquals(expected.message, ex.message)
    }

    @Test
    fun `get document not matching crn`() {
        val crn = "D111111"
        val id = "123-123"
        val document = OffenderDocument()
        document.personId = 10L
        whenever(docPersonRepository.findByCrn(crn)).thenReturn(DocPerson(1L, crn, false, null, null, null))
        whenever(documentRepository.findByAlfrescoIdAndSoftDeletedIsFalse(id)).thenReturn(document)
        val ex = assertThrows<ConflictException> {
            service.getDocument(crn, id)
        }
        assertEquals("Document and CRN do not match", ex.message)
    }
}

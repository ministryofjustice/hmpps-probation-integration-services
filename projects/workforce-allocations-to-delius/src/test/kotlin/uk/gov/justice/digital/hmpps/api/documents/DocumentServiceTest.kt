package uk.gov.justice.digital.hmpps.api.documents

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocEvent
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.EventDocument
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.OffenderDocument
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@ExtendWith(MockitoExtension::class)
class DocumentServiceTest {

    @Mock
    private lateinit var personRepository: PersonRepository

    @Mock
    private lateinit var documentRepository: DocumentRepository

    @InjectMocks
    private lateinit var service: DocumentService

    @Test
    fun `get documents person not found`() {
        val crn = "D111111"
        whenever(personRepository.findByCrn(crn)).thenReturn(null)
        val ex = assertThrows<NotFoundException> {
            service.getDocumentsByCrn(crn)
        }
        val expected = NotFoundException("Person", "crn", crn)
        assertEquals(expected.message, ex.message)
    }

    @Test
    fun `get documents`() {
        val crn = "D111111"
        whenever(personRepository.findByCrn(crn)).thenReturn(Person(1L, crn, false))
        whenever(documentRepository.findAllByPersonId(1L)).thenReturn(listOf(OffenderDocument()))
        val documents = service.getDocumentsByCrn(crn)
        assertEquals(1, documents.size)
    }

    @Test
    fun `get event documents`() {
        val crn = "D111111"
        whenever(personRepository.findByCrn(crn)).thenReturn(Person(1L, crn, false))

        val eventDocuments = listOf(
            EventDocument(
                DocEvent(
                    1L,
                    Person(1L, crn, false),
                    true,
                    "1",
                    null,
                    null
                )
            ),
            EventDocument(
                DocEvent(
                    1L,
                    Person(1L, crn, false),
                    false,
                    "1",
                    null,
                    null
                )
            )
        )

        whenever(documentRepository.findAllByPersonId(1L)).thenReturn(eventDocuments)
        val documents = service.getDocumentsByCrn(crn)
        assertEquals(1, documents.size)
    }
}

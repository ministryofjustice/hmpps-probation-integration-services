import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.entity.CourtReport
import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.exception.NotFoundException

class DocumentTest {

    @Test
    fun `should create Document with correct properties`() {
        val person = mock<Person>()
        val courtReport = mock<CourtReport>()
        val document = Document(
            person = person,
            courtReport = courtReport,
            tableName = "table",
            externalReference = "ref",
            rowVersion = 1L,
            softDeleted = false,
            id = 100L
        )

        assertEquals(person, document.person)
        assertEquals(courtReport, document.courtReport)
        assertEquals("table", document.tableName)
        assertEquals("ref", document.externalReference)
        assertEquals(1L, document.rowVersion)
        assertFalse(document.softDeleted)
        assertEquals(100L, document.id)
    }

    @Test
    fun `psrUrn should format uuid correctly`() {
        val uuid = "abc-123"
        val urn = Document.psrUrn(uuid)
        assertEquals("urn:uk:gov:hmpps:pre-sentence-service:report:abc-123", urn)
    }
}

class DocumentRepositoryTest {

    @Test
    fun `getbyUuid returns document if found`() {
        val repo = mock<DocumentRepository>()
        val doc = mock<Document>()
        whenever(repo.findByExternalReference(any())).thenReturn(doc)
        whenever(repo.getByUuid("uuid")).thenCallRealMethod()
        whenever(repo.findByExternalReference(Document.psrUrn("uuid"))).thenReturn(doc)

        val result = repo.getByUuid("uuid")
        assertEquals(doc, result)
    }

    @Test
    fun `getbyUuid throws NotFoundException if not found`() {
        val repo = mock<DocumentRepository>()
        whenever(repo.findByExternalReference(any())).thenReturn(null)
        whenever(repo.getByUuid("uuid")).thenCallRealMethod()

        val ex = assertThrows(NotFoundException::class.java) {
            repo.getByUuid("uuid")
        }
        assertTrue(ex.message!!.contains("Document with external reference"))
    }

    @Test
    fun `getEventIdByUuid returns eventId if found`() {
        val repo = mock<DocumentRepository>()
        whenever(repo.findEventIdFromDocument(any())).thenReturn(123L)
        whenever(repo.getEventIdByUuid("uuid")).thenCallRealMethod()
        val result = repo.getEventIdByUuid("uuid")
        assertEquals(123L, result)
    }

    @Test
    fun `getEventIdByUuid throws NotFoundException if not found`() {
        val repo = mock<DocumentRepository>()
        whenever(repo.findEventIdFromDocument(any())).thenReturn(null)
        whenever(repo.getEventIdByUuid("uuid")).thenCallRealMethod()
        val ex = assertThrows(NotFoundException::class.java) {
            repo.getEventIdByUuid("uuid")
        }
        assertTrue(ex.message!!.contains("EventId from Document with external reference"))
    }
}

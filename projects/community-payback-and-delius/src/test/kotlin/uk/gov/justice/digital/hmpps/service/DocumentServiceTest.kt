package uk.gov.justice.digital.hmpps.service

import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.client.AlfrescoDocument
import uk.gov.justice.digital.hmpps.client.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class DocumentServiceTest {

    @Mock
    lateinit var documentRepository: DocumentRepository

    @Mock
    lateinit var alfrescoUploadClient: AlfrescoUploadClient

    @Mock
    lateinit var entityManager: EntityManager

    @Mock
    lateinit var query: Query

    @InjectMocks
    lateinit var documentService: DocumentService

    private val person = mock<Person> { on { crn } doReturn "X123456" }
    private val contact = mock<Contact> { on { id } doReturn 99L }
    private val appointment = mock<UnpaidWorkAppointment> {
        on { it.person } doReturn person
        on { it.contact } doReturn contact
    }

    private fun document(alfrescoId: String = "alfresco-id-1") = Document(
        person = person,
        alfrescoId = alfrescoId,
        name = "evidence.pdf",
        primaryKeyId = 99L,
        tableName = "CONTACT",
        externalReference = Document.communityPaybackUrn(UUID.randomUUID()),
        workInProgress = "N",
        status = "Y",
        softDeleted = false,
        id = 7L,
    )

    @BeforeEach
    fun setUp() {
        whenever(entityManager.createNativeQuery(any())).thenReturn(query)
        whenever(query.setParameter(any<String>(), any())).thenReturn(query)
        whenever(documentRepository.save(any<Document>())).thenAnswer { it.arguments[0] as Document }
    }

    @Test
    fun `uploads appointment document successfully`() {
        whenever(alfrescoUploadClient.upload(any())).thenReturn(AlfrescoDocument("alfresco-id-1"))

        val result = documentService.uploadAppointmentDocument(
            appointment, "evidence.pdf", "file-content".toByteArray(), userId = 42L
        )

        assertThat(result.alfrescoId).isEqualTo("alfresco-id-1")
        assertThat(result.name).isEqualTo("evidence.pdf")
        assertThat(result.primaryKeyId).isEqualTo(99L)
        assertThat(result.lastUpdatedUserId).isEqualTo(42L)
        verify(query).setParameter("documentLinked", "Y")
        verify(query).setParameter("contactId", 99L)
    }

    @Test
    fun `throws exception for disallowed file extension and never uploads or saves`() {
        val exception = assertThrows<IllegalArgumentException> {
            documentService.uploadAppointmentDocument(
                appointment, "malware.exe", "file-content".toByteArray(), userId = 42L
            )
        }

        assertThat(exception.message).isEqualTo(
            "File extension 'exe' is not allowed. Allowed extensions: " +
                DocumentService.ALLOWED_EXTENSIONS.joinToString(", ")
        )
        verify(alfrescoUploadClient, never()).upload(any())
        verify(documentRepository, never()).save(any<Document>())
    }

    @Test
    fun `deletes document, removes it from alfresco and unlinks contact when no documents remain`() {
        val document = document()
        whenever(
            documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNotAndSoftDeletedFalse("CONTACT", 99L, 7L)
        ).thenReturn(false)

        documentService.deleteDocument(document)

        verify(alfrescoUploadClient).delete("alfresco-id-1")
        verify(documentRepository).delete(document)
        verify(query).setParameter("documentLinked", "N")
    }

    @Test
    fun `deletes document even when alfresco copy is already missing, keeping contact linked`() {
        val document = document(alfrescoId = "missing-id")
        whenever(alfrescoUploadClient.delete("missing-id")).thenThrow(mock<HttpClientErrorException.NotFound>())
        whenever(
            documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNotAndSoftDeletedFalse("CONTACT", 99L, 7L)
        ).thenReturn(true)

        documentService.deleteDocument(document)

        verify(documentRepository).delete(document)
        verify(query).setParameter("documentLinked", "Y")
    }

    @Test
    fun `validateFile accepts allowed extensions and rejects others`() {
        DocumentService.ALLOWED_EXTENSIONS.forEach { documentService.validateFile("document.$it", ByteArray(0)) }

        assertThrows<IllegalArgumentException> { documentService.validateFile("document.exe", ByteArray(0)) }
    }
}



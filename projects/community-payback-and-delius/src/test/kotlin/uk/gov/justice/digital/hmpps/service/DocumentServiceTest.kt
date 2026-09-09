package uk.gov.justice.digital.hmpps.service

import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.client.AlfrescoDocument
import uk.gov.justice.digital.hmpps.client.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
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

    @Test
    fun `uploads appointment document successfully`() {
        whenever(alfrescoUploadClient.upload(any())).thenReturn(AlfrescoDocument("alfresco-id-1"))
        whenever(documentRepository.save(any<Document>())).thenAnswer { it.arguments[0] as Document }
        whenever(entityManager.createNativeQuery(any())).thenReturn(query)
        whenever(query.setParameter(any<String>(), any())).thenReturn(query)

        val result = documentService.uploadAppointmentDocument(
            appointment,
            "evidence.pdf",
            "file-content".toByteArray(),
            userId = 42L
        )

        assertThat(result.alfrescoId).isEqualTo("alfresco-id-1")
        assertThat(result.name).isEqualTo("evidence.pdf")
        assertThat(result.primaryKeyId).isEqualTo(99L)
        assertThat(result.tableName).isEqualTo("CONTACT")
        assertThat(result.lastUpdatedUserId).isEqualTo(42L)

        verify(documentRepository).save(any<Document>())
        verify(query).setParameter("documentLinked", "Y")
        verify(query).setParameter("contactId", 99L)
    }

    @Test
    fun `throws exception for disallowed file extension`() {
        val exception = assertThrows<IllegalArgumentException> {
            documentService.uploadAppointmentDocument(
                appointment,
                "malware.exe",
                "file-content".toByteArray(),
                userId = 42L
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
    fun `deletes document and marks contact as having no remaining documents`() {
        val document = Document(
            person = person,
            alfrescoId = "alfresco-id-1",
            name = "evidence.pdf",
            primaryKeyId = 99L,
            tableName = "CONTACT",
            externalReference = Document.communityPaybackUrn(java.util.UUID.randomUUID()),
            lastSaved = ZonedDateTime.now(),
            createdDatetime = ZonedDateTime.now(),
            lastUpdatedUserId = 42L,
            workInProgress = "N",
            status = "Y",
            softDeleted = false,
            id = 7L,
        )
        whenever(
            documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNotAndSoftDeletedFalse(
                "CONTACT",
                99L,
                7L
            )
        ).thenReturn(false)
        whenever(entityManager.createNativeQuery(any())).thenReturn(query)
        whenever(query.setParameter(any<String>(), any())).thenReturn(query)

        documentService.deleteDocument(document)

        verify(alfrescoUploadClient).delete("alfresco-id-1")
        verify(documentRepository).delete(document)
        verify(query).setParameter("documentLinked", "N")
        verify(query).setParameter("contactId", 99L)
    }

    @Test
    fun `deletes document even when alfresco document is already missing`() {
        val document = Document(
            person = person,
            alfrescoId = "missing-id",
            name = "evidence.pdf",
            primaryKeyId = 99L,
            tableName = "CONTACT",
            externalReference = Document.communityPaybackUrn(java.util.UUID.randomUUID()),
            lastSaved = ZonedDateTime.now(),
            createdDatetime = ZonedDateTime.now(),
            lastUpdatedUserId = 42L,
            workInProgress = "N",
            status = "Y",
            softDeleted = false,
            id = 7L,
        )
        whenever(alfrescoUploadClient.delete("missing-id")).thenThrow(mock<HttpClientErrorException.NotFound>())
        whenever(
            documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNotAndSoftDeletedFalse(
                "CONTACT",
                99L,
                7L
            )
        ).thenReturn(true)
        whenever(entityManager.createNativeQuery(any())).thenReturn(query)
        whenever(query.setParameter(any<String>(), any())).thenReturn(query)

        documentService.deleteDocument(document)

        verify(documentRepository).delete(document)
        verify(query).setParameter("documentLinked", "Y")
        verify(query).setParameter("contactId", 99L)
    }

    @Test
    fun `validateFile accepts allowed extensions`() {
        DocumentService.ALLOWED_EXTENSIONS.forEach { extension ->
            documentService.validateFile("document.$extension", ByteArray(0))
        }
    }

    @Test
    fun `validateFile rejects disallowed extensions`() {
        assertThrows<IllegalArgumentException> {
            documentService.validateFile("document.exe", ByteArray(0))
        }
    }
}




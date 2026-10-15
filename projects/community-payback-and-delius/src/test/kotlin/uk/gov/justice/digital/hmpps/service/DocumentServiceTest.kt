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
import org.mockito.kotlin.*
import org.mockito.quality.Strictness
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.client.AlfrescoClient
import uk.gov.justice.digital.hmpps.client.AlfrescoDocument
import uk.gov.justice.digital.hmpps.client.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.DocumentUploadResponse
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class DocumentServiceTest {

    @Mock
    lateinit var documentRepository: DocumentRepository

    @Mock
    lateinit var alfrescoClient: AlfrescoClient

    @Mock
    lateinit var alfrescoUploadClient: AlfrescoUploadClient

    @Mock
    lateinit var entityManager: EntityManager

    @Mock
    lateinit var query: Query

    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @InjectMocks
    lateinit var documentService: DocumentService

    private val person = mock<Person> { on { crn } doReturn "X123456" }
    private val contact = mock<Contact> { on { id } doReturn 99L }
    private val appointment = mock<UnpaidWorkAppointment> {
        on { it.person } doReturn person
        on { it.contact } doReturn contact
    }

    private fun document(id: Long = 1L, name: String = "evidence.pdf", alfrescoId: String = "alfresco-id-1") = Document(
        person = person,
        alfrescoId = alfrescoId,
        name = name,
        primaryKeyId = 99L,
        tableName = "CONTACT",
        externalReference = Document.communityPaybackUrn(UUID.randomUUID()),
        workInProgress = "N",
        status = "Y",
        softDeleted = false,
        id = id,
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
        val file = MockMultipartFile("file", "evidence.pdf", "application/pdf", "file-content".toByteArray())

        val result = documentService.uploadAppointmentDocument(appointment, file)

        assertThat(result.alfrescoId).isEqualTo("alfresco-id-1")
        assertThat(result.name).isEqualTo("evidence.pdf")
        assertThat(result.primaryKeyId).isEqualTo(99L)
        verify(alfrescoUploadClient).upload(check {
            assertThat(it.getFirst("fileName")?.body).isEqualTo(file.originalFilename)
            assertThat(it.getFirst("filedata")?.body).isEqualTo(file.bytes)
            assertThat(it.getFirst("CRN")?.body).isEqualTo(person.crn)
            assertThat(it.getFirst("entityId")?.body).isEqualTo("99")
        })
        verify(documentRepository).save(result)
        verify(query).setParameter("documentLinked", "Y")
        verify(query).setParameter("contactId", 99L)
    }

    @Test
    fun `gets documents for an appointment`() {
        whenever(documentRepository.findAllByPrimaryKeyIdAndTableName(99L, "CONTACT")).thenReturn(
            listOf(
                document(id = 1L, name = "evidence.pdf"),
                document(id = 2L, name = "certificate.docx")
            )
        )

        val result = documentService.getDocuments(99L)

        assertThat(result.content).containsExactly(
            DocumentUploadResponse(1L, "evidence.pdf"),
            DocumentUploadResponse(2L, "certificate.docx"),
        )
        verify(documentRepository).findAllByPrimaryKeyIdAndTableName(99L, "CONTACT")
    }

    @Test
    fun `gets empty document list when appointment has no documents`() {
        whenever(documentRepository.findAllByPrimaryKeyIdAndTableName(99L, "CONTACT")).thenReturn(emptyList())

        val result = documentService.getDocuments(99L)

        assertThat(result.content).isEmpty()
        verify(documentRepository).findAllByPrimaryKeyIdAndTableName(99L, "CONTACT")
    }

    @Test
    fun `downloads document`() {
        val document = document()
        val expectedResponse = ResponseEntity<StreamingResponseBody>(HttpStatus.OK)
        whenever(documentRepository.findById(document.id)).thenReturn(Optional.of(document))
        whenever(alfrescoClient.streamDocument(document.alfrescoId, document.name)).thenReturn(expectedResponse)

        val result = documentService.downloadDocument(document.primaryKeyId, document.id)

        assertThat(result).isSameAs(expectedResponse)
        verify(alfrescoClient).streamDocument(document.alfrescoId, document.name)
    }

    @Test
    fun `validateFile accepts allowed extensions and rejects others`() {
        DocumentService.ALLOWED_EXTENSIONS.forEach { documentService.validateFile("document.$it") }

        assertThrows<IllegalArgumentException> { documentService.validateFile("document.exe") }
    }

    @Test
    fun `throws exception for disallowed file extension and never uploads or saves`() {
        val file = MockMultipartFile("file", "malware.exe", "application/octet-stream", "file-content".toByteArray())

        val exception = assertThrows<IllegalArgumentException> {
            documentService.uploadAppointmentDocument(appointment, file)
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
            documentRepository
                .existsByTableNameAndPrimaryKeyIdAndIdNot(document.tableName, document.primaryKeyId, document.id)
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
            documentRepository
                .existsByTableNameAndPrimaryKeyIdAndIdNot(document.tableName, document.primaryKeyId, document.id)
        ).thenReturn(true)

        documentService.deleteDocument(document)

        verify(documentRepository).delete(document)
        verify(query).setParameter("documentLinked", "Y")
    }

    @Test
    fun `deleteDocumentById deletes document when appointment id matches`() {
        val appointmentId = 123L
        val documentId = 456L

        val document = mock<Document> {
            on { id } doReturn documentId
            on { primaryKeyId } doReturn appointmentId
            on { alfrescoId } doReturn "alfresco-123"
            on { tableName } doReturn "CONTACT"
            on { externalReference } doReturn "urn:test"
        }

        whenever(documentRepository.findById(documentId)).thenReturn(Optional.of(document))
        whenever(documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNot("CONTACT", appointmentId, documentId))
            .thenReturn(false)

        documentService.deleteDocumentById(appointmentId, documentId)

        verify(documentRepository).findById(documentId)
        verify(documentRepository).delete(document)
        verify(documentRepository).existsByTableNameAndPrimaryKeyIdAndIdNot("CONTACT", appointmentId, documentId)
    }

    @Test
    fun `deleteDocumentById throws when document does not belong to appointment`() {
        val appointmentId = 123L
        val otherAppointmentId = 999L
        val documentId = 456L

        val document = mock<Document> {
            on { id } doReturn documentId
            on { primaryKeyId } doReturn otherAppointmentId
        }

        whenever(documentRepository.findById(documentId)).thenReturn(Optional.of(document))

        val exception = assertThrows<IllegalArgumentException> {
            documentService.deleteDocumentById(appointmentId, documentId)
        }

        assertThat(exception).hasMessage("Document $documentId does not belong to appointment $appointmentId")
        verify(documentRepository).findById(documentId)
        verify(documentRepository, never()).delete(any())
    }

    @Test
    fun `deleteDocumentById throws when document does not exist`() {
        whenever(documentRepository.findById(456L)).thenReturn(Optional.empty())

        val exception = assertThrows<NotFoundException> {
            documentService.deleteDocumentById(123L, 456L)
        }

        assertThat(exception).hasMessage("Document with id of 456 not found")
        verify(documentRepository).findById(456L)
        verify(documentRepository, never()).delete(any())
        verifyNoInteractions(alfrescoUploadClient, entityManager)
    }
}

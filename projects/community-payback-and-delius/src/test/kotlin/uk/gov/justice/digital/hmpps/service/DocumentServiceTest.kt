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
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
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
}

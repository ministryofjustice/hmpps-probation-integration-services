import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.*
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import uk.gov.justice.digital.hmpps.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.service.DocumentService

class DocumentServiceTest {
    private val documentRepository = mock<DocumentRepository>()
    private val entityManager = mock<EntityManager>()
    private val query = mock<Query>()
    private val documentService = DocumentService(
        auditedInteractionService = mock(),
        documentRepository = documentRepository,
        alfrescoUploadClient = mock(),
        auditUserService = mock(),
        entityManager = entityManager
    )

    @ParameterizedTest
    @ValueSource(strings = [
        "ADDRESSASSESSMENT", "ASSESSMENT", "CONTACT", "NSI", "REFERRAL", "REGISTRATION", "UPW_APPOINTMENT"
    ])
    fun `updateParent sets document_linked correctly for supported tables`(tableName: String) {
        val tableNameToSnakeCase = mapOf(
            "ADDRESSASSESSMENT" to "address_assessment",
            "ASSESSMENT" to "assessment",
            "CONTACT" to "contact",
            "NSI" to "nsi",
            "REFERRAL" to "referral",
            "REGISTRATION" to "registration",
            "UPW_APPOINTMENT" to "upw_appointment"
        )
        val expectedTable = tableNameToSnakeCase[tableName] ?: tableName.lowercase()
        val document = DocumentEntity(
            id = 1L,
            tableName = tableName,
            primaryKeyId = 123L,
            name = "test.pdf",
            alfrescoId = "1234567890",
            externalReference = "urn:example",
            person = mock(),
            status = "Y",
            workInProgress = "N",
            lastSaved = null,
            createdDatetime = null,
            lastUpdatedUserId = null,
            softDeleted = false
        )
        whenever(documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNot(tableName, 123L, 1L)).thenReturn(true)
        whenever(entityManager.createNativeQuery(any())).thenReturn(query)
        whenever(query.setParameter("documentLinked", "Y")).thenReturn(query)
        whenever(query.setParameter("primaryKeyId", 123L)).thenReturn(query)
        whenever(query.executeUpdate()).thenReturn(1)

        documentService.updateParent(document)

        verify(entityManager).createNativeQuery(argThat { queryString -> queryString.contains(expectedTable) })
        verify(query).setParameter("documentLinked", "Y")
        verify(query).setParameter("primaryKeyId", 123L)
        verify(query).executeUpdate()
    }

    @Test
    fun `updateParent sets document_linked to N when no other documents exist`() {
        val document = DocumentEntity(
            id = 1L,
            tableName = "CONTACT",
            primaryKeyId = 123L,
            name = "test.pdf",
            alfrescoId = "1234567890",
            externalReference = "urn:example",
            person = mock(),
            status = "Y",
            workInProgress = "N",
            lastSaved = null,
            createdDatetime = null,
            lastUpdatedUserId = null,
            softDeleted = false
        )
        whenever(documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNot("CONTACT", 123L, 1L)).thenReturn(false)
        whenever(entityManager.createNativeQuery(any())).thenReturn(query)
        whenever(query.setParameter("documentLinked", "N")).thenReturn(query)
        whenever(query.setParameter("primaryKeyId", 123L)).thenReturn(query)
        whenever(query.executeUpdate()).thenReturn(1)

        documentService.updateParent(document)

        verify(query).setParameter("documentLinked", "N")
        verify(query).setParameter("primaryKeyId", 123L)
    }

    @Test
    fun `updateParent does nothing for unsupported table`() {
        val document = DocumentEntity(
            id = 1L,
            tableName = "UNSUPPORTED",
            primaryKeyId = 123L,
            name = "test.pdf",
            alfrescoId = "1234567890",
            externalReference = "urn:example",
            person = mock(),
            status = "Y",
            workInProgress = "N",
            lastSaved = null,
            createdDatetime = null,
            lastUpdatedUserId = null,
            softDeleted = false
        )
        documentService.updateParent(document)
        verify(entityManager, never()).createNativeQuery(any())
        verify(query, never()).setParameter(eq("documentLinked"), any())
        verify(query, never()).setParameter(eq("primaryKeyId"), any())
        verify(query, never()).executeUpdate()
    }
}

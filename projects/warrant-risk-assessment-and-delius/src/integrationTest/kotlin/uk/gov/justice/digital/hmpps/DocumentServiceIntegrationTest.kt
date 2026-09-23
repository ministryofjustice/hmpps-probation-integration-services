package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.entity.Contact
import uk.gov.justice.digital.hmpps.entity.ContactRepository
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.service.DocumentService
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
internal class DocumentServiceIntegrationTest @Autowired constructor(
    private val documentService: DocumentService,
    private val documentRepository: DocumentRepository,
    private val contactRepository: ContactRepository,
    private val entityManager: EntityManager,
    private val wireMockServer: WireMockServer,
) {
    @BeforeEach
    fun setUp() {
        wireMockServer.resetRequests()
    }

    @Test
    fun `deleting unsupported parent table still removes document and calls alfresco`() {
        val wraId = UUID.randomUUID()
        val document = documentRepository.saveAndFlush(
            DocumentGenerator.generateDocument(
                wraId = wraId,
                id = IdGenerator.id(),
                person = PersonGenerator.DEFAULT,
                primaryKeyId = IdGenerator.id(),
                tableName = "EVENT",
            )
        )

        documentService.deleteDocument(deleteMessage(wraId))
        entityManager.clear()

        assertThat(documentRepository.findById(document.id)).isEmpty
        wireMockServer.verify(putRequestedFor(urlEqualTo("/alfresco/release/${document.alfrescoId}")))
        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/alfresco/deletehard/${document.alfrescoId}")))
    }

    @Test
    fun `deleting the last contact document clears document linked flag`() {
        val contact = saveContact(documentLinked = "Y")
        val wraId = UUID.randomUUID()
        val document = documentRepository.saveAndFlush(
            DocumentGenerator.generateDocument(
                wraId = wraId,
                id = IdGenerator.id(),
                person = PersonGenerator.DEFAULT,
                primaryKeyId = contact.id,
                tableName = "CONTACT",
            )
        )

        documentService.deleteDocument(deleteMessage(wraId))
        entityManager.clear()

        assertThat(documentRepository.findById(document.id)).isEmpty
        assertThat(contactRepository.findById(contact.id).get().documentLinked).isEqualTo("N")
    }

    @Test
    fun `deleting one of multiple contact documents keeps document linked flag set`() {
        val contact = saveContact(documentLinked = "Y")
        val deletedWraId = UUID.randomUUID()
        val remainingWraId = UUID.randomUUID()
        val deletedDocument = documentRepository.saveAndFlush(
            DocumentGenerator.generateDocument(
                wraId = deletedWraId,
                id = IdGenerator.id(),
                person = PersonGenerator.DEFAULT,
                primaryKeyId = contact.id,
                tableName = "CONTACT",
            )
        )
        val remainingDocument = documentRepository.saveAndFlush(
            DocumentGenerator.generateDocument(
                wraId = remainingWraId,
                id = IdGenerator.id(),
                person = PersonGenerator.DEFAULT,
                primaryKeyId = contact.id,
                tableName = "CONTACT",
            )
        )

        documentService.deleteDocument(deleteMessage(deletedWraId))
        entityManager.clear()

        assertThat(documentRepository.findById(deletedDocument.id)).isEmpty
        assertThat(documentRepository.findById(remainingDocument.id)).isPresent
        assertThat(contactRepository.findById(contact.id).get().documentLinked).isEqualTo("Y")
    }

    @Test
    fun `upload maps special entity types for alfresco`() {
        val tableMappings = mapOf(
            "APPROVED_PREMISES_REFERRAL" to "APREFERRAL",
            "COURT_REPORT" to "COURTREPORT",
            "INSTITUTIONAL_REPORT" to "INSTITUTIONALREPORT",
            "NSI" to "PROCESSCONTACT",
            "PERSONAL_CIRCUMSTANCE" to "PERSONALCIRCUMSTANCE",
            "UPW_APPOINTMENT" to "UPWAPPOINTMENT",
        )

        tableMappings.forEach { (tableName, entityType) ->
            wireMockServer.resetRequests()
            val wraId = UUID.randomUUID()
            val document = documentRepository.saveAndFlush(
                DocumentGenerator.generateDocument(
                    wraId = wraId,
                    id = IdGenerator.id(),
                    person = PersonGenerator.DEFAULT,
                    primaryKeyId = IdGenerator.id(),
                    tableName = tableName,
                    name = "wra.docx",
                )
            )

            documentService.uploadDocument(createdMessage(wraId), "%PDF-test".toByteArray())
            entityManager.clear()

            assertThat(documentRepository.findById(document.id).get().name).isEqualTo("wra.pdf")
            wireMockServer.verify(
                postRequestedFor(urlEqualTo("/alfresco/uploadnew"))
                    .withRequestBodyPart(aMultipart().withName("entityType").withBody(equalTo(entityType)).build())
                    .withRequestBodyPart(aMultipart().withName("fileName").withBody(equalTo("wra.pdf")).build())
            )
        }
    }

    @Test
    fun `upload fails when audit user cannot be found`() {
        val wraId = UUID.randomUUID()
        val document = documentRepository.saveAndFlush(
            DocumentGenerator.generateDocument(
                wraId = wraId,
                id = IdGenerator.id(),
                person = PersonGenerator.DEFAULT,
                primaryKeyId = IdGenerator.id(),
                tableName = "CONTACT",
            )
        )

        val error = assertThrows<NotFoundException> {
            documentService.uploadDocument(createdMessage(wraId, username = "missing-user"), "%PDF-test".toByteArray())
        }
        entityManager.clear()

        assertThat(error.message).isEqualTo("User with username of missing-user not found")
        assertThat(documentRepository.findById(document.id).get().status).isEqualTo("N")
        wireMockServer.verify(0, anyRequestedFor(urlPathMatching("/alfresco/.*")))
    }

    private fun saveContact(documentLinked: String) = contactRepository.saveAndFlush(
        Contact(
            id = IdGenerator.id(),
            personId = PersonGenerator.DEFAULT.id,
            type = ContactGenerator.HOME_VISIT_TYPE,
            date = LocalDate.now(),
            softDeleted = false,
            documentLinked = documentLinked,
        )
    )

    private fun createdMessage(wraId: UUID, username: String = "officer") = prepEvent("wra-form-created", wireMockServer.port()).message.copy(
        additionalInformation = mapOf(
            "WRAId" to wraId.toString(),
            "username" to username,
        )
    )

    private fun deleteMessage(wraId: UUID) = prepEvent("wra-form-deleted", wireMockServer.port()).message.copy(
        additionalInformation = mapOf(
            "WRAId" to wraId.toString(),
            "username" to "officer",
        )
    )
}


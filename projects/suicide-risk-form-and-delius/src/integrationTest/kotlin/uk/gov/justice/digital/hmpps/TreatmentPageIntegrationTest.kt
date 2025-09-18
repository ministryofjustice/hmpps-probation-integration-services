package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.DocumentRepository
import uk.gov.justice.digital.hmpps.model.ContactDocumentResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
class TreatmentPageIntegrationTest {
    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var wireMockServer: WireMockServer

    @Autowired
    internal lateinit var documentRepository: DocumentRepository

    @Test
    fun `can retrieve documents for a single contact`() {
        val contact = ContactGenerator.generateContact(
            person = ContactGenerator.DEFAULT_CONTACT.person,
            type = ContactGenerator.DEFAULT_CONTACT.type,
            dateTime = ContactGenerator.DEFAULT_CONTACT.startTime!!,
            id = 100L
        )

        val document = DocumentGenerator.generateDocument(
            suicideRiskFormId = DocumentGenerator.SUICIDE_RISK_FORM_ID,
            primaryKeyId = contact.id,
            name = "test1.doc"
        )
        documentRepository.save(document)

        val response = mockMvc.perform(
            post("/treatment")
                .withToken()
                .content("[${contact.id}]")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ContactDocumentResponse>()

        assert(response.content.size == 1)
        assert(response.content[0].id == contact.id)
        assert(response.content[0].documents.size == 1)
        assert(response.content[0].documents[0].name == "test1.doc")
    }

    @Test
    fun `can retrieve documents for multiple contacts`() {
        val contact1 = ContactGenerator.generateContact(
            person = ContactGenerator.DEFAULT_CONTACT.person,
            type = ContactGenerator.DEFAULT_CONTACT.type,
            dateTime = ContactGenerator.DEFAULT_CONTACT.startTime!!,
            id = 101L
        )
        val contact2 = ContactGenerator.generateContact(
            person = ContactGenerator.DEFAULT_CONTACT.person,
            type = ContactGenerator.DEFAULT_CONTACT.type,
            dateTime = ContactGenerator.DEFAULT_CONTACT.startTime!!,
            id = 102L
        )

        val document1 = DocumentGenerator.generateDocument(
            suicideRiskFormId = DocumentGenerator.SUICIDE_RISK_FORM_ID,
            primaryKeyId = contact1.id,
            name = "test2.doc"
        )
        val document2 = DocumentGenerator.generateDocument(
            suicideRiskFormId = DocumentGenerator.SUICIDE_RISK_FORM_ID,
            primaryKeyId = contact2.id,
            name = "test3.doc"
        )
        documentRepository.save(document1)
        documentRepository.save(document2)

        val response = mockMvc.perform(
            post("/treatment")
                .withToken()
                .content("[${contact1.id},${contact2.id}]")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ContactDocumentResponse>()

        assert(response.content.size == 2)
        val item1 = response.content.find { it.id == contact1.id }
        val item2 = response.content.find { it.id == contact2.id }
        assert(item1 != null && item1.documents.any { it.name == "test2.doc" })
        assert(item2 != null && item2.documents.any { it.name == "test3.doc" })
    }

    @Test
    fun `returns empty list for zero contact ids`() {
        val response = mockMvc.perform(
            post("/treatment")
                .withToken()
                .content("[]")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ContactDocumentResponse>()

        assert(response.content.isEmpty())
    }

    @Test
    fun `document array empty for contact with no linked documents`() {
        val contact = ContactGenerator.generateContact(
            person = ContactGenerator.DEFAULT_CONTACT.person,
            type = ContactGenerator.DEFAULT_CONTACT.type,
            dateTime = ContactGenerator.DEFAULT_CONTACT.startTime!!,
            id = 103L
        )

        val response = mockMvc.perform(
            post("/treatment")
                .withToken()
                .content("[${contact.id}]")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ContactDocumentResponse>()

        assert(response.content.size == 1)
        assert(response.content[0].id == contact.id)
        assert(response.content[0].documents.isEmpty())
    }
}
package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.APPT_CT_3
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.generateContact
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDateTime
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class DocumentUploadIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var contactRepository: ContactRepository

    @Autowired
    internal lateinit var documentRepository: DocumentRepository

    @Test
    fun `update contact with document`() {
        val person = OVERVIEW
        val document = ResourceUtils.getFile("classpath:simulations/__files/document.pdf")

        val multipartFile = MockMultipartFile(
            "file", "document.pdf", "application/pdf", document.readBytes()
        )


        val contactToSave = generateContact(
            person,
            APPT_CT_3,
            ZonedDateTime.of(LocalDateTime.now(EuropeLondon), EuropeLondon),
            description = "add document contact"
        )

        val savedContact = contactRepository.save(contactToSave)

        val requestBuilder = multipart("/documents/${person.crn}/update/contact/{id}", savedContact.id)
            .file(multipartFile)
            .with { request ->
                request.method = "PATCH"
                request.setParameter("crn", person.crn)
                request
            }

        mockMvc.perform(requestBuilder.withToken())
            .andExpect(status().isOk)

        //assert doc
        val doc = documentRepository.findByPrimaryKeyId(savedContact.id)

        contactRepository.delete(savedContact)
        documentRepository.delete(doc)

    }
}
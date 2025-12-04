package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.APPT_CT_3
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.PI_USER
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDateTime
import java.time.ZonedDateTime

class DocumentUploadIntegrationTest : IntegrationTestBase() {

    @Test
    fun `update contact with document`() {
        val person = OVERVIEW
        val document = ResourceUtils.getFile("classpath:simulations/__files/document.pdf")

        val multipartFile = MockMultipartFile(
            "file", "document.pdf", "application/pdf", document.readBytes()
        )

        val contactToSave = ContactGenerator.generateContact(
            person,
            APPT_CT_3,
            ZonedDateTime.of(LocalDateTime.now(EuropeLondon), EuropeLondon),
            description = "add document contact"
        )

        val savedContact = contactRepository.save(contactToSave)
        assertNull(documentRepository.findByPrimaryKeyId(savedContact.id))

        val requestBuilder = multipart("/documents/${person.crn}/update/contact/{id}", savedContact.id)
            .file(multipartFile)
            .with { request ->
                request.method = "PATCH"
                request.setParameter("crn", person.crn)
                request
            }

        mockMvc.perform(requestBuilder.withToken())
            .andExpect(status().isOk)

        val doc = documentRepository.findByPrimaryKeyId(savedContact.id)
        assertThat(doc?.alfrescoId, equalTo("00000000-0000-0000-0000-000000000001"))
        assertThat(doc?.name, equalTo(document.name))
        assertThat(doc?.personId, equalTo(person.id))
        assertThat(doc?.primaryKeyId, equalTo(savedContact.id))
        assertThat(doc?.type, equalTo("DOCUMENT"))
        assertThat(doc?.lastUpdatedUserId, equalTo(PI_USER.id))
        assertThat(doc?.createdByUserId, equalTo(PI_USER.id))



        contactRepository.delete(savedContact)
        documentRepository.delete(doc!!)
    }
}
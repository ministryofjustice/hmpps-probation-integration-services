package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateContact
import uk.gov.justice.digital.hmpps.service.DocumentsService
import org.springframework.http.HttpHeaders
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.security.TokenHelper
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.objectMapper
import java.time.ZonedDateTime

class UpdateContactIntegrationTest : IntegrationTestBase() {

    @MockitoBean
    lateinit var documentsService: DocumentsService

    private val contact = ContactGenerator.UPDATABLE_CONTACT
    private val nonUpdatableContact = ContactGenerator.NON_UPDATABLE_CONTACT

    @Test
    fun `update contact without files returns 200`() {
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = "Updated notes",
            sensitiveFlag = null
        )

        val requestPart = MockMultipartFile(
            "request", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        )

        mockMvc.perform(
            multipart(HttpMethod.PATCH, "/contact/${contact.id}")
                .file(requestPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${TokenHelper.TOKEN}")
        ).andExpect(status().isOk)
    }

    @Test
    fun `update contact with a file returns 200`() {
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = "Notes with file",
            sensitiveFlag = null
        )

        val filePart = MockMultipartFile(
            "files", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf content".toByteArray()
        )
        val requestPart = MockMultipartFile(
            "request", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        )

        mockMvc.perform(
            multipart(HttpMethod.PATCH, "/contact/${contact.id}")
                .file(filePart)
                .file(requestPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${TokenHelper.TOKEN}")
        ).andExpect(status().isOk)
    }

    @Test
    fun `update contact with invalid contact id returns 404`() {
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = null,
            sensitiveFlag = null
        )

        val requestPart = MockMultipartFile(
            "request", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        )

        mockMvc.perform(
            multipart(HttpMethod.PATCH, "/contact/999999")
                .file(requestPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${TokenHelper.TOKEN}")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `update contact with type not in allowed enum returns 400`() {
        // NON_UPDATABLE_CONTACT uses APPT_CT_1 (code "C089") which is NOT in CreateContact.Type entries
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = null,
            sensitiveFlag = null
        )

        val requestPart = MockMultipartFile(
            "request", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        )

        mockMvc.perform(
            multipart(HttpMethod.PATCH, "/contact/${nonUpdatableContact.id}")
                .file(requestPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${TokenHelper.TOKEN}")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `update contact notes are appended`() {
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = "Appended note",
            sensitiveFlag = null
        )

        val requestPart = MockMultipartFile(
            "request", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        )

        mockMvc.perform(
            multipart(HttpMethod.PATCH, "/contact/${contact.id}")
                .file(requestPart)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${TokenHelper.TOKEN}")
        ).andExpect(status().isOk)

        val savedContact = contactRepository.findById(contact.id).get()
        assertThat(savedContact.notes?.contains("Appended note"), equalTo(true))
    }
}

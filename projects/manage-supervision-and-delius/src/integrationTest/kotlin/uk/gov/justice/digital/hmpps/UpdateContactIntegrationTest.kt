package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.patch
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateContact
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

class UpdateContactIntegrationTest : IntegrationTestBase() {

    private val contact = ContactGenerator.UPDATABLE_CONTACT
    private val nonUpdatableContact = ContactGenerator.NON_UPDATABLE_CONTACT

    @Test
    fun `update contact returns 200`() {
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = "Updated notes",
            sensitiveFlag = null
        )

        mockMvc.patch("/contact/${contact.id}") {
            withToken()
            json = request
        }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `update contact with invalid contact id returns 404`() {
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = null,
            sensitiveFlag = null
        )

        mockMvc.patch("/contact/987322642") {
            withToken()
            json = request
        }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `update contact with type not in allowed enum returns 400`() {
        // NON_UPDATABLE_CONTACT uses APPT_CT_1 (code "C089") which is NOT in CreateContact.Type entries
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = null,
            sensitiveFlag = null
        )


        mockMvc.patch("/contact/${nonUpdatableContact.id}") {
            withToken()
            json = request
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `update contact notes are appended`() {
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = "Appended note",
            sensitiveFlag = null
        )


        mockMvc.patch("/contact/${contact.id}") {
            withToken()
            json = request
        }
            .andExpect { status { isOk() } }

        val savedContact = contactRepository.findById(contact.id).get()
        assertThat(savedContact.notes!!).contains("Appended note")
    }

    @Test
    fun `sensitive flag is updated but wont clear existing value if set to true`() {
        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = null,
            sensitiveFlag = true
        )

        mockMvc.patch("/contact/${contact.id}") {
            withToken()
            json = request
        }
            .andExpect { status { isOk() } }
        val savedContact = contactRepository.findById(contact.id).get()
        assertThat(savedContact.sensitive!!).isTrue

        val request2 = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = null,
            sensitiveFlag = false
        )

        mockMvc.patch("/contact/${contact.id}") {
            withToken()
            json = request2
        }
            .andExpect { status { isBadRequest() } }
    }
}

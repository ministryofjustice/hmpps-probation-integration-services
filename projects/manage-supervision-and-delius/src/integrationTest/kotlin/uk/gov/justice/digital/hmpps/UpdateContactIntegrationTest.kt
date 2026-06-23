package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.patch
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateContact
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.generateContact
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.generateContactAlert
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
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
            sensitiveFlag = null,
            alert = null
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
            sensitiveFlag = null,
            alert = null
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
            sensitiveFlag = null,
            alert = null
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
            sensitiveFlag = null,
            alert = null
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
            sensitiveFlag = true,
            alert = null
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
            sensitiveFlag = false,
            alert = null
        )

        mockMvc.patch("/contact/${contact.id}") {
            withToken()
            json = request2
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `update contact creates alert when requested`() {
        val alertableContact = contactRepository.save(
            generateContact(
                person = PersonGenerator.OVERVIEW,
                contactType = ContactGenerator.EMAIL_POP_CT,
                startDateTime = ZonedDateTime.now(),
                event = PersonGenerator.EVENT_1
            )
        )

        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = null,
            sensitiveFlag = null,
            alert = true
        )

        mockMvc.patch("/contact/${alertableContact.id}") {
            withToken()
            json = request
        }
            .andExpect { status { isOk() } }

        val savedContact = contactRepository.findById(alertableContact.id).get()
        assertThat(savedContact.alert).isTrue
        assertThat(contactAlertRepository.findByContactId(alertableContact.id)).hasSize(1)
    }

    @Test
    fun `update contact alert request with no active person manager returns not found`() {
        val alertableContact = contactRepository.save(
            generateContact(
                person = PersonGenerator.PRE_SENTENCE_PERSON,
                contactType = ContactGenerator.EMAIL_POP_CT,
                startDateTime = ZonedDateTime.now(),
                event = PersonGenerator.EVENT_1
            )
        )

        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = null,
            sensitiveFlag = null,
            alert = true
        )

        mockMvc.patch("/contact/${alertableContact.id}") {
            withToken()
            json = request
        }
            .andExpect { status { isNotFound() } }

        assertThat(contactAlertRepository.findByContactId(alertableContact.id)).isEmpty()
    }

    @Test
    fun `update contact deletes alert when requested`() {
        val alertedContact = contactRepository.save(
            generateContact(
                person = PersonGenerator.OVERVIEW,
                contactType = ContactGenerator.EMAIL_POP_CT,
                startDateTime = ZonedDateTime.now(),
                alert = true,
                event = PersonGenerator.EVENT_1
            )
        )
        contactAlertRepository.save(generateContactAlert(alertedContact))

        val request = UpdateContact(
            dateTime = ZonedDateTime.now(),
            notes = null,
            sensitiveFlag = null,
            alert = false
        )

        mockMvc.patch("/contact/${alertedContact.id}") {
            withToken()
            json = request
        }
            .andExpect { status { isOk() } }

        val savedContact = contactRepository.findById(alertedContact.id).get()
        assertThat(savedContact.alert).isFalse
        assertThat(contactAlertRepository.findByContactId(alertedContact.id)).isEmpty()
    }
}

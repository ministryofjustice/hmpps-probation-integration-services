package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContact
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContactResponse
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalTime

class ContactLogIntegrationTest : IntegrationTestBase()  {

    @Test
    fun `invalid crn returns not found`() {
        val crn = "X000000"
        
        mockMvc.post("/contact/$crn") {
            withToken()
            json = CreateContact(
                staffId = OffenderManagerGenerator.STAFF_1.id,
                contactType = ContactGenerator.EMAIL_POP_CT.code,
                notes = "Test",
                alert = false,
                sensitive = false,
                visorReport = false
            )
        }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `invalid staff id returns not found`() {
        val invalidStaffId = 99999L

        mockMvc.post("/contact/${PersonGenerator.PERSON_1.crn}") {
            withToken()
            json = CreateContact(
                staffId = invalidStaffId,
                contactType = ContactGenerator.EMAIL_POP_CT.code,
                notes = "Test",
                alert = false,
                sensitive = false,
                visorReport = false
            )
        }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `invalid contact type returns not found`() {
        val invalidContactType = "WXYZ"

        mockMvc.post("/contact/${PersonGenerator.PERSON_1.crn}") {
            withToken()
            json = CreateContact(
                staffId = OffenderManagerGenerator.STAFF_1.id,
                contactType = invalidContactType,
                notes = "Test",
                alert = false,
                sensitive = false,
                visorReport = false
            )
        }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `create a valid contact`() {
        val response = mockMvc.post("/contact/${PersonGenerator.PERSON_1.crn}") {
            withToken()
            json = CreateContact(
                staffId = OffenderManagerGenerator.STAFF_1.id,
                contactType = ContactGenerator.EMAIL_POP_CT.code,
                notes = "Test",
                alert = false,
                sensitive = false,
                visorReport = false
            )
        }
            .andExpect { status { isCreated() } }
            .andReturn().response.contentAsJson<CreateContactResponse>()

        val savedContact = contactRepository.findById(response.id).get()
        assertThat(savedContact.type.code, equalTo(ContactGenerator.EMAIL_POP_CT.code))
        assertThat(savedContact.notes, Matchers.containsString("Test"))
        assertThat(savedContact.notes, Matchers.containsString("This contact was automatically created by the Manage Supervision integrations service."))
        assertThat(savedContact.date, equalTo(LocalDate.now()))
        assertThat(savedContact.startTime, isCloseTo(LocalTime.now()))
        assertThat(savedContact.alert, equalTo(false))
        assertThat(savedContact.sensitive, equalTo(false))
        assertThat(savedContact.isVisor, equalTo(false))

    }

    @Test
    fun `create a valid event-level contact`() {
        val response = mockMvc.post("/contact/${PersonGenerator.PERSON_1.crn}") {
            withToken()
            json = CreateContact(
                staffId = OffenderManagerGenerator.STAFF_1.id,
                contactType = ContactGenerator.EMAIL_POP_CT.code,
                eventId = PersonGenerator.EVENT_1.id,
                alert = false,
                sensitive = false,
                visorReport = false
            )
        }
            .andExpect { status { isCreated() } }
            .andReturn().response.contentAsJson<CreateContactResponse>()

        val savedContact = contactRepository.findById(response.id).get()
        assertThat(savedContact.event?.id, equalTo(PersonGenerator.EVENT_1.id))
    }

    @Test
    fun `create a valid component-level contact`() {
        val response = mockMvc.post("/contact/${PersonGenerator.PERSON_1.crn}") {
            withToken()
            json = CreateContact(
                staffId = OffenderManagerGenerator.STAFF_1.id,
                contactType = ContactGenerator.EMAIL_POP_CT.code,
                eventId = PersonGenerator.EVENT_1.id,
                requirementId = PersonGenerator.REQUIREMENT.id,
                alert = false,
                sensitive = false,
                visorReport = false
            )
        }
            .andExpect { status { isCreated() } }
            .andReturn().response.contentAsJson<CreateContactResponse>()

        val savedContact = contactRepository.findById(response.id).get()
        assertThat(savedContact.requirement?.id, equalTo(PersonGenerator.REQUIREMENT.id))
    }
}
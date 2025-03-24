package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.schedule.PersonAppointment
import uk.gov.justice.digital.hmpps.api.model.schedule.Schedule
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LONG_NOTE
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.service.toActivity
import uk.gov.justice.digital.hmpps.service.toDocument
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ScheduleIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `upcoming schedule is returned`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/schedule/${person.crn}/upcoming").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Schedule>()

        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.appointments[0].id, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().id))
        assertThat(res.appointments[0].type, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().type))
        assertThat(
            res.appointments[0].location?.officeName,
            equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().location?.officeName)
        )
        assertThat(res.appointments[0].location?.postcode, equalTo("H34 7TH"))
    }

    @Test
    fun `previous schedule is returned`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/schedule/${person.crn}/previous").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Schedule>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.appointments[0].id, equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity().id))
        assertThat(
            res.appointments[0].type,
            equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity().type)
        )
        assertThat(
            res.appointments[0].location?.officeName,
            equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity().location?.officeName)
        )
        assertThat(res.appointments[0].location?.postcode, equalTo("H34 7TH"))
    }

    @Test
    fun `previous schedule not found status returned`() {
        mockMvc
            .perform(get("/schedule/X123456/previous").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Upcomimg schedule not found status returned`() {
        mockMvc
            .perform(get("/schedule/X123456/upcoming").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `individual appointment is returned`() {

        val person = OVERVIEW
        val id = ContactGenerator.NEXT_APPT_CONTACT.id
        val res = mockMvc
            .perform(get("/schedule/${person.crn}/appointment/${id}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonAppointment>()
        val expectedDocs =
            listOf(ContactGenerator.CONTACT_DOCUMENT_1.toDocument(), ContactGenerator.CONTACT_DOCUMENT_2.toDocument())
        val expectedAppointment = ContactGenerator.NEXT_APPT_CONTACT.toActivity().copy(documents = expectedDocs)
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.appointment.id, equalTo(expectedAppointment.id))
        assertThat(res.appointment.type, equalTo(expectedAppointment.type))
        assertThat(res.appointment.location?.officeName, equalTo(expectedAppointment.location?.officeName))
        assertThat(res.appointment.documents.size, equalTo(expectedAppointment.documents.size))
        assertThat(res.appointment.location?.postcode, equalTo("H34 7TH"))
        assertThat(res.appointment.description, equalTo(expectedAppointment.description))
    }

    @Test
    fun `individual appointment with outcome is returned`() {

        val person = OVERVIEW
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val res = mockMvc
            .perform(get("/schedule/${person.crn}/appointment/${id}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonAppointment>()

        val expectedNotes = listOf(
            NoteDetail(0, "Tom Brady", LocalDate.of(2024,10, 29), "was on holiday", false),
            NoteDetail(1, "Harry Kane", LocalDate.of(2024,10, 29), LONG_NOTE.substring(0,1500), true)
        )

        assertThat(res.appointment.description, equalTo("previous appointment"))
        assertThat(res.appointment.outcome, equalTo("Acceptable"))
        assertThat(res.appointment.activityNotes, equalTo(expectedNotes))
    }

    @Test
    fun `individual appointment with outcome with single note is returned`() {

        val person = OVERVIEW
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val res = mockMvc
            .perform(get("/schedule/${person.crn}/appointment/${id}/note/1").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonAppointment>()

        val expectedNote = NoteDetail(1, "Harry Kane", LocalDate.of(2024,10, 29), LONG_NOTE)

        assertThat(res.appointment.description, equalTo("previous appointment"))
        assertThat(res.appointment.outcome, equalTo("Acceptable"))
        assertThat(res.appointment.activityNote, equalTo(expectedNote))
    }
}

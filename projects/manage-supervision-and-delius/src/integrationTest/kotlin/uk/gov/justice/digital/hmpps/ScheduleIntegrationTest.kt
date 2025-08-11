package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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

    @ParameterizedTest
    @CsvSource(
        "upcoming,2,0,1",
        "upcoming?size=1,1,0,2",
        "upcoming?sortBy=appointment&ascending=false,2,1,1",
    )
    fun `upcoming schedule is returned`(uri: String, resultSize: Int, element: Int, totalPages: Int) {
        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/schedule/${person.crn}/$uri").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Schedule>()

        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.personSchedule.page, equalTo(0))
        assertThat(res.personSchedule.totalPages, equalTo(totalPages))
        assertThat(res.personSchedule.appointments.size, equalTo(resultSize))
        assertThat(res.personSchedule.appointments[element].id, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().id))
        assertThat(res.personSchedule.appointments[element].type, equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().type))
        assertThat(
            res.personSchedule.appointments[element].location?.officeName,
            equalTo(ContactGenerator.FIRST_APPT_CONTACT.toActivity().location?.officeName)
        )
        assertThat(res.personSchedule.appointments[element].location?.postcode, equalTo("H34 7TH"))
    }

    @ParameterizedTest
    @CsvSource(
        "previous,10,3,5,1",
        "previous?size=4,4,3,4,2"
    )
    fun `previous schedule is returned`(uri: String, requestSize: Int, element: Int, totalResults: Int, totalPages: Int) {
        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/schedule/${person.crn}/$uri").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Schedule>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.personSchedule.page, equalTo(0))
        assertThat(res.personSchedule.size, equalTo(requestSize))
        assertThat(res.personSchedule.appointments.size, equalTo(totalResults))
        assertThat(res.personSchedule.totalPages, equalTo(totalPages))
        assertThat(res.personSchedule.appointments[element].id, equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity().id))
        assertThat(
            res.personSchedule.appointments[element].type,
            equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity().type)
        )
        assertThat(
            res.personSchedule.appointments[element].location?.officeName,
            equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity().location?.officeName)
        )
        assertThat(res.personSchedule.appointments[element].location?.postcode, equalTo("H34 7TH"))
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
            listOf(
                ContactGenerator.CONTACT_DOCUMENT_1.toDocument(),
                ContactGenerator.CONTACT_DOCUMENT_2.toDocument(),
                ContactGenerator.CONTACT_DOCUMENT_3.toDocument()
            )
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
            NoteDetail(0, "Tom Brady", LocalDate.of(2024, 10, 29), "was on holiday", false),
            NoteDetail(1, "Harry Kane", LocalDate.of(2024, 10, 29), LONG_NOTE.substring(0, 1500), true)
        )

        assertThat(res.appointment.description, equalTo("previous appointment"))
        assertThat(res.appointment.outcome, equalTo("Acceptable"))
        assertThat(res.appointment.appointmentNotes, equalTo(expectedNotes))
    }

    @Test
    fun `individual appointment with outcome with single note is returned`() {

        val person = OVERVIEW
        val id = ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
        val res = mockMvc
            .perform(get("/schedule/${person.crn}/appointment/${id}/note/1").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonAppointment>()

        val expectedNote = NoteDetail(1, "Harry Kane", LocalDate.of(2024, 10, 29), LONG_NOTE)

        assertThat(res.appointment.description, equalTo("previous appointment"))
        assertThat(res.appointment.outcome, equalTo("Acceptable"))
        assertThat(res.appointment.appointmentNote, equalTo(expectedNote))
    }
}

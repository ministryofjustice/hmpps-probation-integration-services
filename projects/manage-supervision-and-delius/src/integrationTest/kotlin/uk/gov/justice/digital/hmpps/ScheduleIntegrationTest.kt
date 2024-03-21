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
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.service.toAppointment
import uk.gov.justice.digital.hmpps.service.toDocument
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

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
        assertThat(res.appointments[0], equalTo(ContactGenerator.FIRST_APPT_CONTACT.toAppointment()))
    }

    @Test
    fun `previous schedule is returned`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/schedule/${person.crn}/previous").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Schedule>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.appointments[0], equalTo(ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toAppointment()))

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
        val expectedDocs = listOf(ContactGenerator.CONTACT_DOCUMENT_1.toDocument(), ContactGenerator.CONTACT_DOCUMENT_2.toDocument(),)
        val expectedAppointment = ContactGenerator.NEXT_APPT_CONTACT.toAppointment().copy(documents = expectedDocs)
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.appointment, equalTo(expectedAppointment))

    }
}

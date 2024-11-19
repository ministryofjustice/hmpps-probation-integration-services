package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentDetail
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.Outcome
import uk.gov.justice.digital.hmpps.api.model.appointment.User
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.ATTENDED_COMPLIED
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppointmentOutcomeIntegrationTest {

    @Autowired
    internal lateinit var appointmentRepository: AppointmentRepository

    @Autowired
    internal lateinit var mockMvc: MockMvc

    val outcome = Outcome(123, "Jack Ryan", "ATTC", "N")

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/appointment")
                    .withJson(outcome)
            )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `when an appointment does not exist returns a 404 response`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/appointment")
                    .withToken()
                    .withJson(outcome)
            )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(jsonPath("$.message", equalTo("Appointment with id of 123 not found")))
    }

    @Test
    fun `when an appointment outcome does not exist returns a 404 response`() {
        val response = createAppointment()

        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/appointment")
                    .withToken()
                    .withJson(Outcome(response.appointments[0].id, "Jon Jones", "ABC", "Y"))
            )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(
                jsonPath(
                    "$.message",
                    equalTo("ContactTypeOutcome with contact_type_id 8 and outcome code of ABC not found")
                )
            )

        appointmentRepository.deleteById(response.appointments[0].id)
    }

    @Test
    fun `outcome updated`() {
        val response = createAppointment()
        val createdAppointment = appointmentRepository.findById(response.appointments[0].id).get()

        assertNull(createdAppointment.attended)
        assertNull(createdAppointment.complied)
        assertNull(createdAppointment.notes)
        assertNull(createdAppointment.outcomeId)
        assertNull(createdAppointment.sensitive)

        val request = Outcome(response.appointments[0].id, "Jason Bourne", "ATTC", "Y", notes = "my notes")

        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/appointment")
                    .withToken()
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)

        val updatedAppointment = appointmentRepository.findById(response.appointments[0].id).get()

        assertEquals("Y", updatedAppointment.attended)
        assertEquals("Y", updatedAppointment.complied)
        assertEquals(request.notes, updatedAppointment.notes!!.lines()[1])
        assertEquals(ATTENDED_COMPLIED.id, updatedAppointment.outcomeId)
        assertFalse(updatedAppointment.sensitive!!)

        assertThat(updatedAppointment.type.code, equalTo(createdAppointment.type.code))
        assertThat(updatedAppointment.date, equalTo(createdAppointment.date))
        assertThat(updatedAppointment.startTime, isCloseTo(createdAppointment.startTime))
        assertThat(updatedAppointment.externalReference, equalTo(createdAppointment.externalReference))
        assertThat(updatedAppointment.eventId, equalTo(createdAppointment.eventId))
        assertThat(updatedAppointment.createdByUserId, equalTo(createdAppointment.createdByUserId))
        assertThat(updatedAppointment.staffId, equalTo(createdAppointment.staffId))
        assertThat(updatedAppointment.probationAreaId, equalTo(createdAppointment.probationAreaId))
        assertThat(updatedAppointment.officeLocationId, equalTo(createdAppointment.officeLocationId))

        appointmentRepository.delete(updatedAppointment)
    }

    private fun createAppointment() = mockMvc.perform(
        post("/appointment/${PersonGenerator.PERSON_1.crn}")
            .withToken()
            .withJson(
                CreateAppointment(
                    User(STAFF_USER_1.username, TEAM.description),
                    CreateAppointment.Type.PlannedOfficeVisitNS,
                    ZonedDateTime.now().plusDays(1),
                    ZonedDateTime.now().plusDays(2),
                    eventId = PersonGenerator.EVENT_1.id,
                    uuid = UUID.randomUUID()
                )
            )
    ).andReturn().response.contentAsJson<AppointmentDetail>()
}
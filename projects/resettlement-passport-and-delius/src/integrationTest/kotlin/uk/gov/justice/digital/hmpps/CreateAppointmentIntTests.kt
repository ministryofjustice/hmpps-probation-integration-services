package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.CreateAppointment
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CreateAppointmentIntTests {
    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var appointmentRepository: AppointmentRepository

    @Test
    fun `create appointment when offender does not exist retuns a 404 response`() {
        mockMvc.perform(
            post("/appointments/D123456")
                .withToken()
                .withJson(
                    CreateAppointment(
                        ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(1)
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `create appointment retuns a 409 when conflicting appointment`() {
        val start = ZonedDateTime.now().plusDays(7).plusMinutes(10)
        mockMvc.perform(
            post("/appointments/${PersonGenerator.CREATE_APPOINTMENT.crn}")
                .withToken()
                .withJson(
                    CreateAppointment(start, start.plusMinutes(30))
                )
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `create appointment ending before start returns bad request`() {
        val start = ZonedDateTime.now().plusDays(7).plusMinutes(10)
        mockMvc.perform(
            post("/appointments/${PersonGenerator.CREATE_APPOINTMENT.crn}")
                .withToken()
                .withJson(
                    CreateAppointment(start, start.minusSeconds(1))
                )
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `create a new appointment`() {
        val person = PersonGenerator.CREATE_APPOINTMENT
        val start = ZonedDateTime.now().plusDays(1)
        mockMvc.perform(
            post("/appointments/${person.crn}")
                .withToken()
                .withJson(
                    CreateAppointment(start, start.plusHours(1))
                )
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        val appointment = appointmentRepository.findAppointmentsFor(
            person.crn,
            start.toLocalDate(),
            start.toLocalDate(),
            PageRequest.of(0, 1)
        ).first()

        assertThat(appointment.date, equalTo(start.toLocalDate()))
        assertThat(appointment.startTime, equalTo(start))
        assertThat(appointment.notes, equalTo("Resettlement Passport Notes"))
        assertThat(appointment.description, equalTo("Some Description for RP"))
    }
}
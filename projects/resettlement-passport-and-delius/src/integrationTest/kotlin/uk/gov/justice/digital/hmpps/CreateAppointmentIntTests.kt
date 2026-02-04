package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.CreateAppointment
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.entity.AlertRepository
import uk.gov.justice.digital.hmpps.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.Duration
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CreateAppointmentIntTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val appointmentRepository: AppointmentRepository,
    private val alertRepository: AlertRepository
) {

    @Test
    fun `create appointment when offender does not exist returns a 404 response`() {
        mockMvc.post("/appointments/D123456") {
            withToken()
            json = CreateAppointment(
                CreateAppointment.Type.Accommodation,
                ZonedDateTime.now().plusDays(1),
                Duration.ofMinutes(30)
            )
        }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `create appointment retuns a 409 when conflicting appointment`() {
        val start = ZonedDateTime.now().plusDays(7).plusMinutes(10)
        mockMvc.post("/appointments/${PersonGenerator.CREATE_APPOINTMENT.crn}") {
            withToken()
            json = CreateAppointment(CreateAppointment.Type.Health, start, Duration.ofMinutes(30))
        }
            .andExpect { status { isConflict() } }
    }

    @Test
    fun `create appointment ending before start returns bad request`() {
        val start = ZonedDateTime.now().plusDays(7).plusMinutes(10)
        mockMvc.post("/appointments/${PersonGenerator.CREATE_APPOINTMENT.crn}") {
            withToken()
            json = CreateAppointment(CreateAppointment.Type.Finance, start, Duration.ofMinutes(-1))
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `create a new appointment`() {
        val person = PersonGenerator.CREATE_APPOINTMENT
        val start = ZonedDateTime.now().plusDays(1)
        val notes = "Resettlement Passport Notes"
        val create = CreateAppointment(CreateAppointment.Type.SkillsAndWork, start, Duration.ofHours(1), notes)

        mockMvc.post("/appointments/${person.crn}") {
            withToken()
            json = create
        }
            .andExpect { status { isCreated() } }

        val appointment = appointmentRepository.findAppointmentsFor(
            person.crn,
            start.toLocalDate(),
            start.toLocalDate(),
            PageRequest.of(0, 1)
        ).first()

        assertThat(appointment.type.code, equalTo(create.type.code))
        assertThat(appointment.date, equalTo(start.toLocalDate()))
        assertThat(appointment.startTime!!, isCloseTo(start))
        assertThat(appointment.notes, equalTo(notes))
        assertThat(appointment.externalReference, equalTo(create.urn))

        val alert = alertRepository.findAll().firstOrNull { it.contactId == appointment.id }
        assertNotNull(alert)
    }
}
package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateAppointmentIntegrationTests {

    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var appointmentRepository: AppointmentRepository

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/appointments/D123456"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `when offender does not exist retuns a 404 response`() {
        mockMvc.perform(
            post("/appointments/D123456")
                .withToken()
                .withJson(
                    CreateAppointment(
                        CreateAppointment.Type.HomeVisitToCaseNS,
                        ZonedDateTime.now().plusDays(1),
                        ZonedDateTime.now().plusDays(2),
                        1,
                        1
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `create appointment ending before start returns bad request`() {
        mockMvc.perform(
            post("/appointments/${PersonGenerator.PERSON_1.crn}")
                .withToken()
                .withJson(
                    CreateAppointment(
                        CreateAppointment.Type.InitialAppointmentInOfficeNS,
                        ZonedDateTime.now().plusDays(2),
                        ZonedDateTime.now().plusDays(1),
                        1,
                        1)
                )
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `create a new appointment`() {
        val person = PersonGenerator.PERSON_1
        val start = ZonedDateTime.now().plusDays(1)
        val end = ZonedDateTime.now().plusDays(2)
        val create = CreateAppointment(
            CreateAppointment.Type.PlannedOfficeVisitNS,
            start,
            end,
            1,
            PersonGenerator.EVENT_1.id
            )

        mockMvc.perform(
            post("/appointments/${person.crn}")
                .withToken()
                .withJson(create)
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        val appointment = appointmentRepository.findAppointmentsFor(
            person.crn,
            start.toLocalDate(),
            end.toLocalDate(),
            PageRequest.of(0, 1)
        ).first()

        assertThat(appointment.type.code, equalTo(create.type.code))
        assertThat(appointment.date, equalTo(start.toLocalDate()))
        assertThat(appointment.startTime, isCloseTo(start))
        assertThat(appointment.externalReference, equalTo(create.urn))
        assertThat(appointment.eventId, equalTo(create.eventId))

        appointmentRepository.delete(appointment)
    }
}
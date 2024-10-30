package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.CreatedAppointment
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
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
    fun `appointment end date before start returns bad request`() {
        mockMvc.perform(
            post("/appointments/${PersonGenerator.PERSON_1.crn}")
                .withToken()
                .withJson(
                    CreateAppointment(
                        CreateAppointment.Type.InitialAppointmentInOfficeNS,
                        ZonedDateTime.now().plusDays(2),
                        ZonedDateTime.now().plusDays(1),
                        1,
                        PersonGenerator.EVENT_1.id
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @ParameterizedTest
    @MethodSource("createAppointments")
    fun `create a new appointment`(createAppointment: CreateAppointment) {
        val person = PersonGenerator.PERSON_1

        val response = mockMvc.perform(
            post("/appointments/${person.crn}")
                .withToken()
                .withJson(createAppointment)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn().response.contentAsJson<CreatedAppointment>()

        val appointment = appointmentRepository.findById(response.id).get()

        assertThat(appointment.type.code, equalTo(createAppointment.type.code))
        assertThat(appointment.date, equalTo(createAppointment.start.toLocalDate()))
        assertThat(appointment.startTime, isCloseTo(createAppointment.start))
        assertThat(appointment.externalReference, equalTo(createAppointment.urn))
        assertThat(appointment.eventId, equalTo(createAppointment.eventId))

        appointmentRepository.delete(appointment)
    }

    companion object {
        @JvmStatic
        fun createAppointments() = listOf(
            CreateAppointment(
                CreateAppointment.Type.PlannedOfficeVisitNS,
                ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().plusDays(2),
                1,
                PersonGenerator.EVENT_1.id
            ),
            CreateAppointment(
                CreateAppointment.Type.InitialAppointmentInOfficeNS,
                ZonedDateTime.now().plusDays(1),
                null,
                1,
                PersonGenerator.EVENT_1.id
            )
        )
    }
}
package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentDetail
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.User
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateOverlappingAppointmentIntegrationTest {

    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var appointmentRepository: AppointmentRepository

    private val user = User(STAFF_USER_1.username, TEAM.description)

    @Test
    fun `create overlapping appointment`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.HomeVisitToCaseNS,
            ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.NOON, EuropeLondon),
            ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.NOON.plusHours(1), EuropeLondon),
            numberOfAppointments = 3,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = UUID.randomUUID()
        )

        val response = mockMvc.perform(
            post("/appointment/${PersonGenerator.PERSON_1.crn}")
                .withToken()
                .withJson(appointment)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn().response.contentAsJson<AppointmentDetail>()

        val dateNowPlusOneDay = LocalDate.now().plusDays(1).format(DeliusDateFormatter)
        val dateNowPlusTwoDays = LocalDate.now().plusDays(2).format(DeliusDateFormatter)
        val dateNowPlusThreeDays = LocalDate.now().plusDays(3).format(DeliusDateFormatter)

        val errorMsg = """
            Appointment(s) conflicts with an existing future appointment [{"start":"$dateNowPlusOneDay 12:00","end":"$dateNowPlusOneDay 13:00"},{"start":"$dateNowPlusTwoDays 12:00","end":"$dateNowPlusTwoDays 13:00"},{"start":"$dateNowPlusThreeDays 12:00","end":"$dateNowPlusThreeDays 13:00"}]
        """.trimIndent()
        mockMvc.perform(
            post("/appointment/${PersonGenerator.PERSON_1.crn}")
                .withToken()
                .withJson(appointment)
        )
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(jsonPath("$.message", equalTo(errorMsg)))

        val overlapAppointment = CreateAppointment(
            user,
            CreateAppointment.Type.HomeVisitToCaseNS,
            ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.NOON, EuropeLondon),
            ZonedDateTime.of(LocalDate.now().plusDays(1), LocalTime.NOON.plusHours(1), EuropeLondon),
            numberOfAppointments = 3,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = UUID.randomUUID(),
            createOverlappingAppointment = true
        )

        val overlapAppointmentResponse = mockMvc.perform(
            post("/appointment/${PersonGenerator.PERSON_1.crn}")
                .withToken()
                .withJson(overlapAppointment)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn().response.contentAsJson<AppointmentDetail>()

        val appointments =
            appointmentRepository.findAllById(response.appointments.map { it.id }) + appointmentRepository.findAllById(
                overlapAppointmentResponse.appointments.map { it.id })

        assertThat(appointments.size, equalTo(6))
        appointmentRepository.deleteAll(appointments)
    }


}
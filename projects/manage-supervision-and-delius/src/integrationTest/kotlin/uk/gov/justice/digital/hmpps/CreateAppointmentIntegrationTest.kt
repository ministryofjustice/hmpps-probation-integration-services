package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateAppointmentIntegrationTest {

    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var appointmentRepository: AppointmentRepository

    private val user = User(STAFF_USER_1.username, TEAM.code)

    private val person = PersonGenerator.PERSON_1

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(post("/appointment/D123456"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `when offender does not exist returns a 404 response`() {
        mockMvc.perform(
            post("/appointment/D123456")
                .withToken()
                .withJson(
                    CreateAppointment(
                        user,
                        CreateAppointment.Type.HomeVisitToCaseNS,
                        ZonedDateTime.now().plusDays(1),
                        ZonedDateTime.now().plusDays(2),
                        interval = CreateAppointment.Interval.DAY,
                        numberOfAppointments = 1,
                        eventId = 1,
                        UUID.randomUUID()
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `appointment end date before start returns bad request`() {
        mockMvc.perform(
            post("/appointment/${PersonGenerator.PERSON_1.crn}")
                .withToken()
                .withJson(
                    CreateAppointment(
                        user,
                        CreateAppointment.Type.InitialAppointmentInOfficeNS,
                        ZonedDateTime.now().plusDays(2),
                        ZonedDateTime.now().plusDays(1),
                        interval = CreateAppointment.Interval.DAY,
                        numberOfAppointments = 1,
                        PersonGenerator.EVENT_1.id,
                        UUID.randomUUID()
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(jsonPath("$.message", equalTo("Appointment end time cannot be before start time")))
    }

    @ParameterizedTest
    @MethodSource("createAppointment")
    fun `create a new appointment`(createAppointment: CreateAppointment) {
        val response = mockMvc.perform(
            post("/appointment/${person.crn}")
                .withToken()
                .withJson(createAppointment)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn().response.contentAsJson<AppointmentDetail>()

        val appointment = appointmentRepository.findById(response.appointments[0].id).get()

        assertThat(appointment.type.code, equalTo(createAppointment.type.code))
        assertThat(appointment.date, equalTo(createAppointment.start.toLocalDate()))
        assertThat(appointment.startTime, isCloseTo(createAppointment.start))
        assertThat(appointment.externalReference, equalTo(createAppointment.urn))
        assertThat(appointment.eventId, equalTo(createAppointment.eventId))
        assertThat(appointment.createdByUserId, equalTo(STAFF_USER_1.id))
        assertThat(appointment.staffId, equalTo(STAFF_1.id))
        assertThat(appointment.probationAreaId, equalTo(DEFAULT_PROVIDER.id))
        assertThat(appointment.officeLocationId, equalTo(DEFAULT_LOCATION.id))


        appointmentRepository.delete(appointment)
    }

    @ParameterizedTest
    @MethodSource("createMultipleAppointments")
    fun `create multiple appointments`(createAppointment: CreateAppointment) {
        val response = mockMvc.perform(
            post("/appointment/${person.crn}")
                .withToken()
                .withJson(createAppointment)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn().response.contentAsJson<AppointmentDetail>()

        val appointments = appointmentRepository.findAllById(response.appointments.map { it.id })

        assertThat(appointments.size, equalTo(3))

        assertThat(appointments[0].date, equalTo(LocalDate.now()))
        assertThat(
            appointments[1].date,
            equalTo(LocalDate.now().plusDays(createAppointment.interval.value.toLong() * 1))
        )
        assertThat(
            appointments[2].date,
            equalTo(LocalDate.now().plusDays(createAppointment.interval.value.toLong() * 2))
        )

        //check for unique external reference
        val externalRef = "urn:uk:gov:hmpps:manage-supervision-service:appointment:${createAppointment.uuid}"
        assertThat(appointments[0].externalReference, equalTo(externalRef))
        assertNotEquals(externalRef, appointments[1].externalReference)
        assertNotEquals(externalRef, appointments[2].externalReference)

        appointmentRepository.deleteAll(appointments)
    }

    companion object {
        private val user = User(STAFF_USER_1.username, TEAM.code, DEFAULT_LOCATION.code)

        @JvmStatic
        fun createAppointment() = listOf(
            CreateAppointment(
                user,
                CreateAppointment.Type.PlannedOfficeVisitNS,
                ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().plusDays(1).plusHours(1),
                eventId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            ),
            CreateAppointment(
                user,
                CreateAppointment.Type.InitialAppointmentInOfficeNS,
                ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().plusDays(1).plusHours(1),
                CreateAppointment.Interval.DAY,
                eventId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            ),
            CreateAppointment(
                user,
                CreateAppointment.Type.PlannedDoorstepContactNS,
                ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().plusDays(1).plusHours(1),
                CreateAppointment.Interval.DAY,
                uuid = UUID.randomUUID()
            )
        )

        @JvmStatic
        fun createMultipleAppointments() = listOf(
            CreateAppointment(
                user,
                CreateAppointment.Type.HomeVisitToCaseNS,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusHours(1),
                numberOfAppointments = 3,
                eventId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            ),
            CreateAppointment(
                user,
                CreateAppointment.Type.HomeVisitToCaseNS,
                ZonedDateTime.now(),
                end = ZonedDateTime.now().plusHours(1),
                until = ZonedDateTime.now().plusDays(2),
                eventId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            ),
            CreateAppointment(
                user,
                CreateAppointment.Type.HomeVisitToCaseNS,
                start = ZonedDateTime.now(),
                end = ZonedDateTime.now().plusHours(2),
                until = ZonedDateTime.now().plusDays(14),
                interval = CreateAppointment.Interval.WEEK,
                eventId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            )
        )
    }
}
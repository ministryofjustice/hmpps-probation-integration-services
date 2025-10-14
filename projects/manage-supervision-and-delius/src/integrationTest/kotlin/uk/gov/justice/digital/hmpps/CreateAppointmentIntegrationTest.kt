package uk.gov.justice.digital.hmpps

import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentDetail
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.User
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.PI_USER
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class CreateAppointmentIntegrationTest : IntegrationTestBase() {

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
                        type = CreateAppointment.Type.HomeVisitToCaseNS.code,
                        start = ZonedDateTime.now().plusDays(1),
                        end = ZonedDateTime.now().plusDays(2),
                        interval = CreateAppointment.Interval.DAY,
                        numberOfAppointments = 1,
                        eventId = 1,
                        uuid = UUID.randomUUID()
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
                        type = CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
                        start = ZonedDateTime.now().plusDays(2),
                        end = ZonedDateTime.now().plusDays(1),
                        interval = CreateAppointment.Interval.DAY,
                        numberOfAppointments = 1,
                        eventId = PersonGenerator.EVENT_1.id,
                        uuid = UUID.randomUUID()
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(jsonPath("$.message", equalTo("Appointment end time cannot be before start time")))
    }

    @Test
    fun `number of appointments set to 0`() {
        mockMvc.perform(
            post("/appointment/${PersonGenerator.PERSON_1.crn}")
                .withUserToken(PI_USER.username)
                .withJson(
                    CreateAppointment(
                        user,
                        type = CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
                        start = ZonedDateTime.now().plusDays(1),
                        end = ZonedDateTime.now().plusDays(1).plusHours(1),
                        interval = CreateAppointment.Interval.DAY,
                        numberOfAppointments = 0,
                        eventId = PersonGenerator.EVENT_1.id,
                        uuid = UUID.randomUUID()
                    )
                )
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                jsonPath(
                    "$.fields[0].message",
                    equalTo("number of appointments must be greater than or equal to 1")
                )
            )
    }

    @ParameterizedTest
    @MethodSource("createAppointment")
    fun `create a new appointment without notes`(createAppointment: CreateAppointment) {
        val user = PI_USER

        val response = mockMvc.perform(
            post("/appointment/${PersonGenerator.PERSON_1.crn}")
                .withUserToken(user.username)
                .withJson(createAppointment)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn().response.contentAsJson<AppointmentDetail>()

        val appointment = appointmentRepository.findById(response.appointments[0].id).get()
        assertThat(response.appointments.all { it.externalReference != null }, equalTo(true))

        assertThat(appointment.type.code, equalTo(createAppointment.type))
        assertThat(appointment.date, equalTo(createAppointment.start.toLocalDate()))
        assertThat(appointment.startTime, isCloseTo(createAppointment.start))
        assertThat(appointment.externalReference, equalTo(createAppointment.urn))
        assertThat(appointment.eventId, equalTo(createAppointment.eventId))
        assertThat(appointment.staffId, equalTo(STAFF_1.id))
        assertThat(appointment.probationAreaId, equalTo(DEFAULT_PROVIDER.id))
        assertThat(appointment.officeLocationId, equalTo(DEFAULT_LOCATION.id))
        assertThat(appointment.nsiId, equalTo(createAppointment.nsiId))
        assertThat(appointment.notes, equalTo(createAppointment.notes))
        assertThat(appointment.sensitive, equalTo(createAppointment.sensitive))

        assertThat(appointment.createdByUserId, equalTo(user.id))
        assertThat(appointment.lastUpdatedUserId, equalTo(user.id))

        appointmentRepository.deleteById(appointment.id)
    }

    @ParameterizedTest
    @MethodSource("createMultipleAppointments")
    fun `create multiple appointments`(createAppointment: CreateAppointment) {
        val response = mockMvc.perform(
            post("/appointment/${PersonGenerator.PERSON_1.crn}")
                .withUserToken("DeliusUser")
                .withJson(createAppointment)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn().response.contentAsJson<AppointmentDetail>()

        val appointments = appointmentRepository.findAllById(response.appointments.map { it.id })

        assertThat(appointments.size, equalTo(3))
        assertThat(response.appointments.all { it.externalReference != null }, equalTo(true))

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
            Arguments.of(
                CreateAppointment(
                    user,
                    type = CreateAppointment.Type.PlannedOfficeVisitNS.code,
                    start = ZonedDateTime.now().plusDays(1),
                    end = ZonedDateTime.now().plusDays(1).plusHours(1),
                    eventId = PersonGenerator.EVENT_1.id,
                    uuid = UUID.randomUUID()
                )
            ),
            Arguments.of(
                CreateAppointment(
                    user,
                    type = CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
                    start = ZonedDateTime.now().plusDays(1),
                    end = ZonedDateTime.now().plusDays(1).plusHours(1),
                    interval = CreateAppointment.Interval.DAY,
                    eventId = PersonGenerator.EVENT_1.id,
                    notes = "Some Notes",
                    uuid = UUID.randomUUID()
                )
            ),
            Arguments.of(
                CreateAppointment(
                    user,
                    type = CreateAppointment.Type.PlannedDoorstepContactNS.code,
                    start = ZonedDateTime.now().plusDays(1),
                    end = ZonedDateTime.now().plusDays(1).plusHours(1),
                    interval = CreateAppointment.Interval.DAY,
                    notes = "Some Notes",
                    sensitive = true,
                    uuid = UUID.randomUUID()
                )
            )
        )

        @JvmStatic
        fun createMultipleAppointments() = listOf(
            CreateAppointment(
                user,
                type = CreateAppointment.Type.HomeVisitToCaseNS.code,
                start = ZonedDateTime.now(),
                end = ZonedDateTime.now().plusHours(1),
                numberOfAppointments = 3,
                eventId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            ),
            CreateAppointment(
                user,
                type = CreateAppointment.Type.HomeVisitToCaseNS.code,
                start = ZonedDateTime.now(),
                end = ZonedDateTime.now().plusHours(1),
                until = ZonedDateTime.now().plusDays(2),
                eventId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            ),
            CreateAppointment(
                user,
                CreateAppointment.Type.HomeVisitToCaseNS.code,
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
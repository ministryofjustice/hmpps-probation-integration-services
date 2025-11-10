package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.appointment.RecreateAppointmentRequest
import uk.gov.justice.digital.hmpps.api.model.appointment.RecreatedAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.RescheduleAppointmentRequest
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.ATTENDED_COMPLIED
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.LOCATION_BRK_1
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.STAFF_1
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.PI_USER
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.Appointment
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.getAppointment
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.UUID

class RecreateAppointmentIntegrationTest : IntegrationTestBase() {

    @Test
    fun `end time must be after start time`() {
        val request = recreateRequest(startTime = LocalTime.now().plusHours(1), endTime = LocalTime.now().minusHours(1))
        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/appointments/${IdGenerator.getAndIncrement()}/recreate")
                    .withUserToken(PI_USER.username)
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `recreated appointment must be in the future`() {
        val person = PersonGenerator.RECREATE_APPT_PERSON_1
        val appointment = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().plusDays(1).plusMinutes(30)
            )
        )
        val now = ZonedDateTime.now()
        val request = recreateRequest(
            date = now.toLocalDate(),
            startTime = now.toLocalTime().minusHours(1),
            endTime = now.toLocalTime()
        )

        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/appointments/${appointment.id}/recreate")
                    .withUserToken(PI_USER.username)
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `appointment must be without an outcome to recreate`() {
        val person = PersonGenerator.RECREATE_APPT_PERSON_1
        val appointment = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().minusDays(1).plusHours(1),
                outcome = ATTENDED_COMPLIED
            )
        )
        val request = recreateRequest(startTime = LocalTime.now(), endTime = LocalTime.now().minusHours(1))

        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/appointments/${appointment.id}/recreate")
                    .withUserToken(PI_USER.username)
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `date or time must change to recreate appointment`() {
        val person = PersonGenerator.RECREATE_APPT_PERSON_1
        val start = ZonedDateTime.now().plusDays(2)
        val end = ZonedDateTime.now().plusDays(2).plusMinutes(30)
        val appointment =
            sentenceAppointmentRepository.save(AppointmentGenerator.generateAppointment(person, start, end))
        val request = recreateRequest(
            date = appointment.date,
            startTime = start.toLocalTime(),
            endTime = end.toLocalTime()
        )

        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/appointments/${appointment.id}/recreate")
                    .withUserToken(PI_USER.username)
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `cannot reschedule if appointment would clash`() {
        val person = PersonGenerator.RECREATE_APPT_PERSON_1
        val appointment = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(3),
                ZonedDateTime.now().plusDays(3).plusMinutes(30)
            )
        )
        val clashing = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(4),
                ZonedDateTime.now().plusDays(4).plusMinutes(60)
            )
        )
        val request = recreateRequest(
            date = clashing.date,
            startTime = ZonedDateTime.now().plusDays(4).toLocalTime(),
            endTime = ZonedDateTime.now().plusDays(4).plusHours(1).toLocalTime(),
        )

        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/appointments/${appointment.id}/recreate")
                    .withUserToken(PI_USER.username)
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `recreate with a location`() {
        val person = PersonGenerator.RECREATE_APPT_PERSON_1
        val original = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(5),
                ZonedDateTime.now().plusDays(5).plusMinutes(30),
                notes = "Notes on the original appointment"
            )
        )
        val request = recreateRequest(locationCode = DEFAULT_LOCATION.code, notes = "Notes to be appended")

        val recreated = mockMvc
            .perform(
                MockMvcRequestBuilders.put("/appointments/${original.id}/recreate")
                    .withUserToken(PI_USER.username)
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<RecreatedAppointment>()

        val appointment = appointmentRepository.getAppointment(recreated.id)
        assertThat(appointment.lastUpdatedUserId).isEqualTo(PI_USER.id)
        assertThat(appointment.location?.code).isEqualTo(request.locationCode)
        assertThat(appointment.date).isEqualTo(request.date)
        assertThat(appointment.notes).isEqualTo(
            """
            |${original.notes}
            |
            |Location set to ${DEFAULT_LOCATION.description}
            |
            |${request.notes}
        """.trimMargin()
        )
        assertThat(recreated.externalReference).isEqualTo(Appointment.URN_PREFIX + request.uuid)
        assertThat(appointment.externalReference).isEqualTo(Appointment.URN_PREFIX + request.uuid)
    }

    @Test
    fun `recreate with change in team staff and location`() {
        val person = PersonGenerator.RECREATE_APPT_PERSON_2
        val original = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(6),
                ZonedDateTime.now().plusDays(6).plusMinutes(30),
                locationId = DEFAULT_LOCATION.id,
                notes = "Notes on the original appointment"
            )
        )
        val request = recreateRequest(
            staffCode = STAFF_1.code,
            teamCode = TEAM_1.code,
            locationCode = LOCATION_BRK_1.code,
            notes = "Notes to be appended"
        )

        val recreated = mockMvc
            .perform(
                MockMvcRequestBuilders.put("/appointments/${original.id}/recreate")
                    .withUserToken(PI_USER.username)
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<RecreatedAppointment>()

        val appointment = appointmentRepository.getAppointment(recreated.id)
        assertThat(appointment.lastUpdatedUserId).isEqualTo(PI_USER.id)
        assertThat(appointment.location?.code).isEqualTo(request.locationCode)
        assertThat(appointment.team.code).isEqualTo(request.teamCode)
        assertThat(appointment.staff.code).isEqualTo(request.staffCode)
        assertThat(appointment.date).isEqualTo(request.date)
        assertThat(appointment.notes).isEqualTo(
            """
            |${original.notes}
            |
            |Location changed from ${DEFAULT_LOCATION.description} to ${LOCATION_BRK_1.description}
            |
            |${request.notes}
        """.trimMargin()
        )
        assertThat(recreated.externalReference).isEqualTo(Appointment.URN_PREFIX + request.uuid)
        assertThat(appointment.externalReference).isEqualTo(Appointment.URN_PREFIX + request.uuid)
    }

    @Test
    fun `recreate with sensitive notes`() {
        val person = PersonGenerator.RECREATE_APPT_PERSON_1
        val original = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(7),
                ZonedDateTime.now().plusDays(7).plusMinutes(30),
                notes = "Notes on the original appointment"
            )
        )
        val request = recreateRequest(
            date = LocalDate.now().plusDays(3),
            sensitive = true,
            notes = "Some sensitive notes to append"
        )

        val recreated = mockMvc
            .perform(
                MockMvcRequestBuilders.put("/appointments/${original.id}/recreate")
                    .withUserToken(PI_USER.username)
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<RecreatedAppointment>()

        val appointment = appointmentRepository.getAppointment(recreated.id)
        assertThat(appointment.lastUpdatedUserId).isEqualTo(PI_USER.id)
        assertThat(appointment.sensitive).isEqualTo(request.sensitive)
        assertThat(appointment.date).isEqualTo(request.date)
        assertThat(appointment.notes).isEqualTo(
            """
            |${original.notes}
            |
            |${request.notes}
        """.trimMargin()
        )
        assertThat(recreated.externalReference).isEqualTo(Appointment.URN_PREFIX + request.uuid)
        assertThat(appointment.externalReference).isEqualTo(Appointment.URN_PREFIX + request.uuid)
    }

    @Test
    fun `recreate with non sensitive notes from a sensitive appointment`() {
        val person = PersonGenerator.RECREATE_APPT_PERSON_2
        val original = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(8),
                ZonedDateTime.now().plusDays(8).plusMinutes(30),
                notes = "Notes on the original appointment",
                sensitive = true
            )
        )
        val request = recreateRequest(
            date = LocalDate.now().plusDays(3),
            sensitive = false,
            notes = "Some sensitive notes to append"
        )

        val recreated = mockMvc
            .perform(
                MockMvcRequestBuilders.put("/appointments/${original.id}/recreate")
                    .withUserToken(PI_USER.username)
                    .withJson(request)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<RecreatedAppointment>()

        val appointment = appointmentRepository.getAppointment(recreated.id)
        assertThat(appointment.lastUpdatedUserId).isEqualTo(PI_USER.id)
        assertThat(appointment.sensitive).isEqualTo(true)
        assertThat(appointment.date).isEqualTo(request.date)
        assertThat(appointment.notes).isEqualTo(
            """
            |${original.notes}
            |
            |${request.notes}
        """.trimMargin()
        )
        assertThat(recreated.externalReference).isEqualTo(Appointment.URN_PREFIX + request.uuid)
        assertThat(appointment.externalReference).isEqualTo(Appointment.URN_PREFIX + request.uuid)
    }

    private fun recreateRequest(
        date: LocalDate = LocalDate.now().plusDays(1),
        startTime: LocalTime = LocalTime.now().plusHours(1),
        endTime: LocalTime = LocalTime.now().plusHours(2),
        staffCode: String? = null,
        teamCode: String? = null,
        locationCode: String? = null,
        notes: String? = null,
        sensitive: Boolean? = null,
        sendToVisor: Boolean? = null,
        requestedBy: RecreateAppointmentRequest.RequestedBy = RecreateAppointmentRequest.RequestedBy.SERVICE,
        uuid: UUID? = UUID.randomUUID(),
    ) = RecreateAppointmentRequest(
        date,
        startTime,
        endTime,
        staffCode,
        teamCode,
        locationCode,
        notes,
        sensitive,
        sendToVisor,
        requestedBy,
        uuid
    )
}
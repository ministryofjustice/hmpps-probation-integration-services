package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.api.model.appointment.RescheduleAppointmentRequest
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.LOCATION_BRK_1
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.STAFF_1
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.PI_USER
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.getAppointment
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

class RescheduleAppointmentIntegrationTest : IntegrationTestBase() {

    @Test
    fun `end time must be after start time`() {
        val request =
            rescheduleRequest(startTime = LocalTime.now().plusHours(1), endTime = LocalTime.now().minusHours(1))
        mockMvc.put("/appointments/${IdGenerator.getAndIncrement()}/reschedule") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `rescheduled appointment must be in the future`() {
        val person = PersonGenerator.RESCHEDULED_PERSON_1
        val appointment = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().plusDays(1).plusMinutes(30)
            )
        )
        val now = ZonedDateTime.now()
        val request = rescheduleRequest(
            date = now.toLocalDate(),
            startTime = now.toLocalTime().minusHours(1),
            endTime = now.toLocalTime()
        )

        mockMvc.put("/appointments/${appointment.id}/reschedule") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `appointment must be in the future without an outcome to reschedule`() {
        val person = PersonGenerator.RESCHEDULED_PERSON_1
        val appointment = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusHours(1)
            )
        )
        val request = rescheduleRequest(startTime = LocalTime.now(), endTime = LocalTime.now().minusHours(1))

        mockMvc.put("/appointments/${appointment.id}/reschedule") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `date or time must change to reschedule appointment`() {
        val person = PersonGenerator.RESCHEDULED_PERSON_1
        val start = ZonedDateTime.now().plusDays(2)
        val end = ZonedDateTime.now().plusDays(2).plusMinutes(30)
        val appointment =
            sentenceAppointmentRepository.save(AppointmentGenerator.generateAppointment(person, start, end))
        val request = rescheduleRequest(
            date = appointment.date,
            startTime = start.toLocalTime(),
            endTime = end.toLocalTime()
        )

        mockMvc.put("/appointments/${appointment.id}/reschedule") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `cannot reschedule if appointment would clash`() {
        val person = PersonGenerator.RESCHEDULED_PERSON_1
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
        val request = rescheduleRequest(
            date = clashing.date,
            startTime = ZonedDateTime.now().plusDays(4).toLocalTime(),
            endTime = ZonedDateTime.now().plusDays(4).plusHours(1).toLocalTime(),
        )

        mockMvc.put("/appointments/${appointment.id}/reschedule") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isConflict() } }
    }

    @Test
    fun `set a location where one was not set as part of rescheduling`() {
        val person = PersonGenerator.RESCHEDULED_PERSON_1
        val original = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(5),
                ZonedDateTime.now().plusDays(5).plusMinutes(30),
                notes = "Notes on the original appointment"
            )
        )
        val request = rescheduleRequest(locationCode = DEFAULT_LOCATION.code, notes = "Notes to be appended")

        mockMvc.put("/appointments/${original.id}/reschedule") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isOk() } }

        val appointment = appointmentRepository.getAppointment(original.id!!)
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
    }

    @Test
    fun `update team staff and location`() {
        val person = PersonGenerator.RESCHEDULED_PERSON_2
        val original = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(6),
                ZonedDateTime.now().plusDays(6).plusMinutes(30),
                locationId = DEFAULT_LOCATION.id,
                notes = "Notes on the original appointment"
            )
        )
        val request = rescheduleRequest(
            staffCode = STAFF_1.code,
            teamCode = TEAM_1.code,
            locationCode = LOCATION_BRK_1.code,
            notes = "Notes to be appended"
        )

        mockMvc.put("/appointments/${original.id}/reschedule") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isOk() } }

        val appointment = appointmentRepository.getAppointment(original.id!!)
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
    }

    @Test
    fun `amend sensitive notes to an appointment`() {
        val person = PersonGenerator.RESCHEDULED_PERSON_1
        val original = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(7),
                ZonedDateTime.now().plusDays(7).plusMinutes(30),
                notes = "Notes on the original appointment"
            )
        )
        val request = rescheduleRequest(
            date = LocalDate.now().plusDays(3),
            sensitive = true,
            notes = "Some sensitive notes to append"
        )

        mockMvc.put("/appointments/${original.id}/reschedule") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isOk() } }

        val appointment = appointmentRepository.getAppointment(original.id!!)
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
    }

    @Test
    fun `amend non sensitive notes to a sensitive appointment`() {
        val person = PersonGenerator.RESCHEDULED_PERSON_2
        val original = sentenceAppointmentRepository.save(
            AppointmentGenerator.generateAppointment(
                person,
                ZonedDateTime.now().plusDays(8),
                ZonedDateTime.now().plusDays(8).plusMinutes(30),
                notes = "Notes on the original appointment",
                sensitive = true
            )
        )
        val request = rescheduleRequest(
            date = LocalDate.now().plusDays(3),
            sensitive = false,
            notes = "Some sensitive notes to append"
        )

        mockMvc.put("/appointments/${original.id}/reschedule") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isOk() } }

        val appointment = appointmentRepository.getAppointment(original.id!!)
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
    }

    private fun rescheduleRequest(
        date: LocalDate = LocalDate.now().plusDays(1),
        startTime: LocalTime = LocalTime.now().plusHours(1),
        endTime: LocalTime = LocalTime.now().plusHours(2),
        staffCode: String? = null,
        teamCode: String? = null,
        locationCode: String? = null,
        notes: String? = null,
        sensitive: Boolean? = null,
    ) = RescheduleAppointmentRequest(date, startTime, endTime, staffCode, teamCode, locationCode, notes, sensitive)
}
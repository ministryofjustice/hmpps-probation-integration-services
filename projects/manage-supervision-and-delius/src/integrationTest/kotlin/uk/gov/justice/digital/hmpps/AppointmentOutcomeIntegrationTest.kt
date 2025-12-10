package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentDetail
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.Outcome
import uk.gov.justice.digital.hmpps.api.model.appointment.User
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.ATTENDED_COMPLIED
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.PI_USER
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import java.time.ZonedDateTime
import java.util.*

class AppointmentOutcomeIntegrationTest : IntegrationTestBase() {
    val outcome = Outcome(123, true, "Some notes", false)

    @Test
    fun `unauthorized status returned`() {
        mockMvc.patch("/appointment") { json = outcome }
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `when an appointment does not exist returns a 404 response`() {
        mockMvc.patch("/appointment") {
            withUserToken(PI_USER.username)
            json = outcome
        }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Appointment with id of 123 not found") }
            }
    }

    @Test
    fun `outcome updated`() {
        val response = createAppointment()
        val createdAppointment = sentenceAppointmentRepository.findById(response.appointments[0].id).get()

        assertNull(createdAppointment.attended)
        assertNull(createdAppointment.complied)
        assertNull(createdAppointment.notes)
        assertNull(createdAppointment.outcomeId)
        assertNull(createdAppointment.sensitive)

        val request = Outcome(response.appointments[0].id, true, "my notes", true)

        mockMvc.patch("/appointment") {
            withUserToken(PI_USER.username)
            json = request
        }
            .andExpect { status { isOk() } }

        val updatedAppointment = sentenceAppointmentRepository.findById(response.appointments[0].id).get()

        assertEquals("Y", updatedAppointment.attended)
        assertEquals("Y", updatedAppointment.complied)
        assertEquals(request.notes, updatedAppointment.notes)
        assertEquals(ATTENDED_COMPLIED.id, updatedAppointment.outcomeId)
        assertTrue(updatedAppointment.sensitive!!)

        assertThat(updatedAppointment.type.code, equalTo(createdAppointment.type.code))
        assertThat(updatedAppointment.date, equalTo(createdAppointment.date))
        assertThat(updatedAppointment.startTime, isCloseTo(createdAppointment.startTime!!))
        assertThat(updatedAppointment.externalReference, equalTo(createdAppointment.externalReference))
        assertThat(updatedAppointment.eventId, equalTo(createdAppointment.eventId))
        assertThat(updatedAppointment.createdByUserId, equalTo(createdAppointment.createdByUserId))
        assertThat(updatedAppointment.staffId, equalTo(createdAppointment.staffId))
        assertThat(updatedAppointment.probationAreaId, equalTo(createdAppointment.probationAreaId))
        assertThat(updatedAppointment.officeLocationId, equalTo(createdAppointment.officeLocationId))

        sentenceAppointmentRepository.delete(updatedAppointment)
    }

    private fun createAppointment() = mockMvc.post("/appointment/${PersonGenerator.PERSON_1.crn}") {
        withUserToken(PI_USER.username)
        json =
            CreateAppointment(
                User(STAFF_USER_1.username, TEAM.code, locationCode = DEFAULT_LOCATION.code),
                type = CreateAppointment.Type.PlannedOfficeVisitNS.code,
                start = ZonedDateTime.now().plusDays(1),
                end = ZonedDateTime.now().plusDays(2),
                eventId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            )
    }
        .andReturn().response.contentAsJson<AppointmentDetail>()
}
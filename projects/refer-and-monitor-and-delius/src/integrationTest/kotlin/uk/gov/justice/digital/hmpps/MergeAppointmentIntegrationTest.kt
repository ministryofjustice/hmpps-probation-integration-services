package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.MergeAppointment
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.service.Attended
import uk.gov.justice.digital.hmpps.service.NoSessionReasonType
import uk.gov.justice.digital.hmpps.service.Outcome
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class MergeAppointmentIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var contactRepository: ContactRepository

    private fun makeRequest(person: Person, referralId: UUID, request: MergeAppointment, result: ResultMatcher) {
        mockMvc.perform(
            put("/probation-case/${person.crn}/referrals/$referralId/appointments")
                .withToken()
                .withJson(request)
        )
            .andExpect(result)
    }

    @Test
    @Order(1)
    fun `creates appointment with external reference`() {
        val person = PersonGenerator.NO_APPOINTMENTS
        val referralId = UUID.fromString("09c62549-bcd3-49a9-8120-7811b76925e5")
        val start = ZonedDateTime.now().plusDays(14)
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "RE1234F",
            start,
            30,
            "Appointment Notes",
            "DEFAULT",
            false,
            null,
            null,
            null,
            null,
            null,
        )
        val result = status().isOk
        makeRequest(person, referralId, mergeAppointment, result)

        val appointment = assertDoesNotThrow {
            contactRepository.findByPersonCrnAndExternalReference(
                person.crn,
                mergeAppointment.urn
            )
        }
        assertNotNull(appointment)
        assertThat(appointment!!.date, equalTo(mergeAppointment.start.toLocalDate()))
        assertThat(appointment.startTime, isCloseTo(mergeAppointment.start))
        assertThat(appointment.endTime!!, isCloseTo(mergeAppointment.end))
    }

    @Test
    @Order(2)
    fun `cannot save appointment that conflicts with existing appointment`() {
        val referralId = UUID.fromString("09c62549-bcd3-49a9-8120-7811b76925e5")
        val start = ZonedDateTime.now().plusDays(14).plusMinutes(10)
        val person = PersonGenerator.NO_APPOINTMENTS
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "RE1234F",
            start,
            30,
            "Appointment Notes",
            "DEFAULT",
            false,
            null,
            null,
            null,
            null,
            null,
        )

        makeRequest(person, referralId, mergeAppointment, status().isConflict)
    }

    @Test
    @Order(3)
    fun `can reschedule appointment`() {
        val person = PersonGenerator.NO_APPOINTMENTS
        val referralId = UUID.fromString("09c62549-bcd3-49a9-8120-7811b76925e5")
        val start = ZonedDateTime.now().plusDays(14).plusMinutes(10)
        val appointmentId =
            contactRepository.findAll()
                .filter { it.person.id == person.id }
                .mapNotNull { it.externalReference }
                .map { UUID.fromString(it.substring(it.lastIndexOf(":") + 1, it.length)) }
                .first()

        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "RE1234F",
            start,
            20,
            "Appointment Notes",
            "DEFAULT",
            false,
            null,
            null,
            appointmentId,
            null,
            "Joe Bloggs",
        )
        val result = status().isOk

        makeRequest(person, referralId, mergeAppointment, result)

        val existing =
            contactRepository.findByPersonCrnAndExternalReference(person.crn, mergeAppointment.previousUrn!!)!!
        assertThat(existing.outcome?.code, equalTo(ContactOutcome.Code.RESCHEDULED_POP_REQUEST.value))
        assertFalse(existing.attended!!)
        assertThat(existing.rarActivity, equalTo(false))

        val replacement = assertDoesNotThrow {
            contactRepository.findByPersonCrnAndExternalReference(
                person.crn,
                mergeAppointment.urn
            )
        }

        assertNotNull(replacement)
        assertThat(replacement!!.date, equalTo(mergeAppointment.start.toLocalDate()))
        assertThat(replacement.startTime, isCloseTo(mergeAppointment.start))
        assertThat(replacement.endTime!!, isCloseTo(mergeAppointment.end))
    }

    @Test
    @Order(4)
    fun `can reschedule appointment and apply outcome`() {
        val person = PersonGenerator.NO_APPOINTMENTS
        val referralId = UUID.fromString("09c62549-bcd3-49a9-8120-7811b76925e5")
        val start = ZonedDateTime.now().minusMinutes(30)
        val appointmentId =
            contactRepository.findAll()
                .filter { it.person.id == person.id }
                .mapNotNull { it.externalReference }
                .map { UUID.fromString(it.substring(it.lastIndexOf(":") + 1, it.length)) }
                .first()

        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "RE1234F",
            start,
            30,
            "Appointment Notes",
            "DEFAULT",
            true,
            Outcome(Attended.YES, true, null, false),
            null,
            appointmentId,
            null,
            "Service Provider",
        )
        val result = status().isOk

        makeRequest(person, referralId, mergeAppointment, result)

        val replacement = assertDoesNotThrow {
            contactRepository.findByPersonCrnAndExternalReference(
                person.crn,
                mergeAppointment.urn
            )
        }
        assertNotNull(replacement)
        assertThat(replacement!!.date, equalTo(mergeAppointment.start.toLocalDate()))
        assertThat(replacement.startTime, isCloseTo(mergeAppointment.start))
        assertThat(replacement.endTime!!, isCloseTo(mergeAppointment.end))
        assertNotNull(replacement.outcome)
        assertThat(replacement.attended, equalTo(true))
        assertThat(replacement.hoursCredited, equalTo(0.5))
        assertThat(replacement.rarActivity, equalTo(true))

        val previousAppt =
            contactRepository.findByPersonCrnAndExternalReference(person.crn, mergeAppointment.previousUrn!!)!!
        assertThat(previousAppt.outcome?.code, equalTo(ContactOutcome.Code.RESCHEDULED_SERVICE_REQUEST.value))
        assertFalse(previousAppt.attended!!)
        assertThat(previousAppt.rarActivity, equalTo(false))
    }

    @Test
    fun `cannot save past appointment without an outcome`() {
        val person = PersonGenerator.NO_APPOINTMENTS
        val referralId = UUID.fromString("09c62549-bcd3-49a9-8120-7811b76925e5")
        val start = ZonedDateTime.now().minusDays(1)
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "RE1234F",
            start,
            30,
            "Appointment Notes",
            "DEFAULT",
            false,
            null,
            null,
            null,
            null,
            null,
        )

        makeRequest(person, referralId, mergeAppointment, status().isBadRequest)
    }

    @Test
    @Order(5)
    fun `creates appointment with outcome when in past`() {
        val person = PersonGenerator.NO_APPOINTMENTS
        val referralId = UUID.fromString("09c62549-bcd3-49a9-8120-7811b76925e5")
        val start = ZonedDateTime.now().plusDays(2)
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "RE1234F",
            start,
            30,
            "Appointment Notes",
            "DEFAULT",
            false,
            Outcome(Attended.LATE, false, NoSessionReasonType.POP_UNACCEPTABLE, true),
            null,
            null,
            null,
            null,
        )
        val result = status().isOk

        makeRequest(person, referralId, mergeAppointment, result)

        val appointment = assertDoesNotThrow {
            contactRepository.findByPersonCrnAndExternalReference(
                person.crn,
                mergeAppointment.urn
            )
        }
        assertNotNull(appointment)
        assertThat(appointment!!.date, equalTo(mergeAppointment.start.toLocalDate()))
        assertThat(appointment.startTime, isCloseTo(mergeAppointment.start))
        assertThat(appointment.endTime!!, isCloseTo(mergeAppointment.end))
        assertNotNull(appointment.outcome)
        assertThat(appointment.attended, equalTo(true))
        assertNull(appointment.hoursCredited)
        assertThat(appointment.rarActivity, equalTo(false))
    }

    @Test
    fun `when nsi not found unable to create appointment`() {
        val person = PersonGenerator.NO_APPOINTMENTS
        val referralId = UUID.fromString("10b45127-abc1-29c1-7492-2354c21735b8")
        val start = ZonedDateTime.now().plusDays(10)
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "NE1234D",
            start,
            30,
            "Appointment Notes",
            "DEFAULT",
            false,
            null,
            null,
            null,
            null,
            null,
        )

        makeRequest(person, referralId, mergeAppointment, status().isNotFound)
    }
}

package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit.SECONDS
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest
class AppointmentsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val contactAlertRepository: ContactAlertRepository,
    private val enforcementRepository: EnforcementRepository,
    private val eventRepository: EventRepository,
    private val contactRepository: ContactRepository
) {
    @Test
    fun `non-existent project returns 404`() {
        mockMvc.get("/projects/DOESNOTEXIST/appointments/123?username=DefaultUser") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Project with code of DOESNOTEXIST not found")
            }
    }

    @Test
    fun `can retrieve appointment details`() {
        val response =
            mockMvc.get("/projects/N01DEFAULT/appointments/${UPWGenerator.DEFAULT_UPW_APPOINTMENT.id}?username=DefaultUser") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<AppointmentResponse>()

        assertThat(response.version).isEqualTo(
            UUID(
                UPWGenerator.DEFAULT_UPW_APPOINTMENT.rowVersion,
                UPWGenerator.DEFAULT_CONTACT.rowVersion
            )
        )
        assertThat(response.project.name).isEqualTo("Default UPW Project")
        assertThat(response.case.crn).isEqualTo(PersonGenerator.DEFAULT_PERSON.crn)
        assertThat(response.penaltyHours).isEqualTo("01:05")
        assertThat(response.enforcementAction!!.respondBy).isEqualTo(response.date.plusDays(ReferenceDataGenerator.ROM_ENFORCEMENT_ACTION.responseByPeriod!!))
        assertThat(response.behaviour).isEqualTo(Behaviour.EXCELLENT)
        assertThat(response.workQuality).isEqualTo(WorkQuality.EXCELLENT)
    }

    @Test
    fun `can retrieve single session details`() {
        val response = mockMvc
            .get("/projects/N01SECOND/appointments?date=${LocalDate.now().plusDays(1)}&username=DefaultUser") {
                withToken()
            }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<SessionResponse>()
        assertThat(response.project.name).isEqualTo("Second UPW Project")
        assertThat(response.appointmentSummaries.size).isEqualTo(2)
        assertThat(response.appointmentSummaries[0].case.crn).isEqualTo("Z000001")
        assertThat(response.appointmentSummaries[0].requirementProgress.requiredMinutes).isEqualTo(120 * 60)
        assertThat(response.appointmentSummaries[0].requirementProgress.adjustments).isEqualTo(4)
    }

    @Test
    fun `not updated if version does not match`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id)

        mockMvc.put("/projects/${original.project.code}/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id,
                version = UUID(original.rowVersion + 1, original.contact.rowVersion + 1),
                outcome = Code("A"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(11, 0),
                endTime = LocalTime.of(15, 0),
                notes = "new notes",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 5,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = false,
            )
        }.andExpect { status { isConflict() } }

        val appointment =
            unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id)
        assertThat(appointment.rowVersion).isEqualTo(original.rowVersion)
    }

    @Test
    fun `can update appointment outcome`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id)

        mockMvc.put("/projects/N01DEFAULT/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id,
                version = UUID(original.rowVersion, original.contact.rowVersion),
                outcome = Code("A"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(11, 0),
                endTime = LocalTime.of(15, 0),
                notes = "new notes",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 5,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = false,
            )
        }.andExpect { status { is2xxSuccessful() } }

        val appointment =
            unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id)

        assertThat(appointment).isNotNull
        assertThat(appointment.startTime).isEqualTo(LocalTime.of(11, 0))
        assertThat(appointment.endTime).isEqualTo(LocalTime.of(15, 0))
        assertThat(appointment.lastUpdatedDatetime).isCloseTo(appointment.lastUpdatedDatetime, within(1, SECONDS))
        assertThat(appointment.penaltyTime).isEqualTo(5)
        assertThat(appointment.minutesCredited).isEqualTo(235)

        // tidy up
        unpaidWorkAppointmentRepository.delete(appointment)
    }

    @Test
    fun `contact alert created when alertActive is true`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id)

        mockMvc.put("/projects/N01SECOND/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id,
                version = UUID(original.rowVersion, original.contact.rowVersion),
                outcome = Code("A"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "contact alert",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }.andExpect { status { is2xxSuccessful() } }
        val alert = contactAlertRepository.findAll()
            .firstOrNull { it.contactId == UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.contact.id }
        assertThat(alert).isNotNull
    }

    @Test
    fun `contact alert deleted when alertActive is false`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id)

        mockMvc.put("/projects/N01SECOND/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id,
                version = UUID(original.rowVersion, original.contact.rowVersion),
                outcome = Code("A"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "contact alert",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }.andExpect { status { is2xxSuccessful() } }

        val alertCreated = contactAlertRepository.findAll()

        val second = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id)

        mockMvc.put("/projects/N01SECOND/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id,
                version = UUID(second.rowVersion, second.contact.rowVersion),
                outcome = Code("A"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "contact alert deleting",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = false,
            )
        }.andExpect { status { is2xxSuccessful() } }

        val alertDeleted = contactAlertRepository.findAll()

        assertThat(alertCreated).isNotEmpty
        assertThat(alertDeleted).isEmpty()
    }

    @Test
    fun `enforcement created when complied is false`() {
        val original =
            unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id)

        mockMvc.put("/projects/N01DEFAULT/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id,
                version = UUID(original.rowVersion, original.contact.rowVersion),
                outcome = Code("F"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "enforcement",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }.andExpect { status { is2xxSuccessful() } }

        val enforcement = enforcementRepository.findAll()
            .firstOrNull { it.contact.id == UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.contact.id }
        assertThat(enforcement).isNotNull
    }

    @Test
    fun `returns 2xx when limited access check passes but with current restriction flag true`() {
        val appointmentId = UPWGenerator.LAO_RESTRICTED_UPW_APPOINTMENT.id
        val response =
            mockMvc.get("/projects/N01DEFAULT/appointments/$appointmentId?username=LimitedAccess") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<AppointmentResponse>()
        assertThat(response.case.currentRestriction).isEqualTo(true)
        assertThat(response.case.restrictionMessage).isNotNull()
    }

    @Test
    fun `returns 2xx when limited access check fails but with current restriction flag true`() {
        val appointmentId = UPWGenerator.LAO_RESTRICTED_UPW_APPOINTMENT.id
        val response =
            mockMvc.get("/projects/N01DEFAULT/appointments/$appointmentId?username=FullAccess") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<AppointmentResponse>()
        assertThat(response.case.currentRestriction).isEqualTo(false)
        assertThat(response.case.restrictionMessage.isNullOrEmpty())
    }

    @Test
    fun `returns 2xx when limited access check fails but with current exclusion flag true`() {
        val appointmentId = UPWGenerator.LAO_EXCLUDED_UPW_APPOINTMENT.id

        val response =
            mockMvc.get("/projects/N01DEFAULT/appointments/$appointmentId?username=LimitedAccess") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<AppointmentResponse>()

        assertThat(response.case.currentExclusion).isEqualTo(true)
        assertThat(response.case.exclusionMessage).isNotNull
    }

    @Test
    fun `404 if appointment id is invalid`() {
        mockMvc.put("/projects/N01DEFAULT/appointments/987654/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = 987654,
                version = UUID(1, 1),
                outcome = null,
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "doesn't exist",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.EXCELLENT,
                sensitive = false,
                alertActive = true,
            )
        }
            .andExpect { status { is4xxClientError() } }
    }

    @Test
    fun `ftc count is updated if complied is false`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id)

        mockMvc.put("/projects/N01SECOND/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.id,
                version = UUID(original.rowVersion, original.contact.rowVersion),
                outcome = Code("F"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "ftc count",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }.andExpect { status { is2xxSuccessful() } }

        val event = eventRepository.findAll()
            .firstOrNull { it.id == UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.contact.event!!.id }

        assertThat(event!!.ftcCount).isEqualTo(1L)

        val enforcementReviewContact = contactRepository.findAll()
            .firstOrNull {
                it.linkedContactId == UPWGenerator.UPW_APPOINTMENT_NO_OUTCOME.contact.id
                    && it.contactType.code == ContactType.Code.REVIEW_ENFORCEMENT_STATUS.value
            }
        assertThat(enforcementReviewContact).isNotNull
    }
}
package uk.gov.justice.digital.hmpps.appointments

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.data.entity.EnforcementRepository
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.UPW_PROJECT_2
import uk.gov.justice.digital.hmpps.entity.contact.ContactRepository
import uk.gov.justice.digital.hmpps.entity.contact.ContactType.Code.REVIEW_ENFORCEMENT_STATUS
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.getAppointment
import uk.gov.justice.digital.hmpps.model.AppointmentOutcomeRequest
import uk.gov.justice.digital.hmpps.model.Behaviour
import uk.gov.justice.digital.hmpps.model.Code
import uk.gov.justice.digital.hmpps.model.WorkQuality
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import uk.gov.justice.digital.hmpps.test.TestData
import java.time.LocalTime
import java.time.temporal.ChronoUnit.SECONDS
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class UpdateAppointmentIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val contactAlertRepository: ContactAlertRepository,
    private val enforcementRepository: EnforcementRepository,
    private val eventRepository: EventRepository,
    private val contactRepository: ContactRepository,
) {
    companion object {
        val PROJECT = UPW_PROJECT_2.code
    }

    @Test
    fun `404 if appointment id is invalid`() {
        mockMvc
            .put("/projects/$PROJECT/appointments/9999/outcome") {
                withToken()
                json = TestData.updateAppointment(9999)
            }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Unpaid Work Appointment with id of 9999 not found")
            }
    }

    @Test
    fun `not updated if version does not match`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[0].id)

        mockMvc
            .put("/projects/$PROJECT/appointments/${original.id}/outcome") {
                withToken()
                json = TestData.updateAppointment(original.id).copy(
                    version = UUID(original.rowVersion + 1, original.contact.rowVersion + 1)
                )
            }
            .andExpect { status { isConflict() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("optimistic locking failed")
            }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment.rowVersion).isEqualTo(original.rowVersion)
    }

    @Test
    fun `can update appointment outcome`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[1].id)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}/outcome") {
            withToken()
            json = TestData.updateAppointment(original.id)
        }.andExpect { status { isOk() } }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment).isNotNull
        assertThat(appointment.date).isEqualTo(original.date)
        assertThat(appointment.startTime).isEqualTo(LocalTime.of(10, 0))
        assertThat(appointment.endTime).isEqualTo(LocalTime.of(18, 0))
        assertThat(appointment.penaltyMinutes).isEqualTo(65)
        assertThat(appointment.minutesCredited).isEqualTo(415)
        assertThat(appointment.project.code).isEqualTo(PROJECT)
        assertThat(appointment.notes).isEqualTo("testing update")
        assertThat(appointment.lastUpdatedDatetime).isCloseTo(appointment.lastUpdatedDatetime, within(1, SECONDS))
    }

    @Test
    fun `ftc count is updated if complied is false`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id)
        val originalContact = original.contact
        val originalFtcCount = originalContact.event!!.ftcCount

        // Clear any existing enforcement review contacts
        contactRepository.deleteAll(contactRepository.findAll().filter {
            it.event?.id == originalContact.event!!.id && it.contactType.code == REVIEW_ENFORCEMENT_STATUS.value
        })

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = original.id,
                version = UUID(original.rowVersion, originalContact.rowVersion),
                outcome = Code("F"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "ftc count",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                minutesCredited = 55,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }.andExpect { status { is2xxSuccessful() } }

        val event = eventRepository.findAll().first { it.id == originalContact.event!!.id }
        assertThat(event.ftcCount).isEqualTo(originalFtcCount + 1)

        val enforcementReviewContact = contactRepository.findAll().filter {
            it.linkedContactId == originalContact.id && it.contactType.code == REVIEW_ENFORCEMENT_STATUS.value
        }
        assertThat(enforcementReviewContact).hasSize(1)
    }

    @Test
    fun `enforcement created when complied is false`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[2].id)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = original.id,
                version = UUID(original.rowVersion, original.contact.rowVersion),
                outcome = Code("F"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "enforcement",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                minutesCredited = 375,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }.andExpect { status { is2xxSuccessful() } }

        val enforcement = enforcementRepository.findAll().firstOrNull { it.contact.id == original.contact.id }
        assertThat(enforcement).isNotNull
    }

    @Test
    fun `contact alert created when alertActive is true`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[3].id)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = original.id,
                version = UUID(original.rowVersion, original.contact.rowVersion),
                outcome = Code("A"),
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "contact alert",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                minutesCredited = 55,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }.andExpect { status { is2xxSuccessful() } }
        val alert = contactAlertRepository.findAll().firstOrNull { it.contactId == original.contact.id }
        assertThat(alert).isNotNull
    }

    @Test
    fun `contact alert deleted when alertActive is false`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[4].id)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = original.id,
                version = UUID(original.rowVersion, original.contact.rowVersion),
                outcome = null,
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "contact alert",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                minutesCredited = 55,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = true,
            )
        }.andExpect { status { is2xxSuccessful() } }

        val alertCreated = contactAlertRepository.findAll().filter { it.contactId == original.contact.id }

        val second = unpaidWorkAppointmentRepository.getAppointment(original.id)

        mockMvc.put("/projects/$PROJECT/appointments/${second.id}/outcome") {
            withToken()
            json = AppointmentOutcomeRequest(
                id = second.id,
                version = UUID(second.rowVersion, second.contact.rowVersion),
                outcome = null,
                supervisor = Code("N01P001"),
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(10, 0),
                notes = "contact alert deleting",
                hiVisWorn = true,
                workedIntensively = true,
                penaltyMinutes = 65,
                minutesCredited = 55,
                workQuality = WorkQuality.EXCELLENT,
                behaviour = Behaviour.UNSATISFACTORY,
                sensitive = false,
                alertActive = false,
            )
        }.andExpect { status { is2xxSuccessful() } }

        val alertDeleted = contactAlertRepository.findAll().filter { it.contactId == original.contact.id }

        assertThat(alertCreated).isNotEmpty
        assertThat(alertDeleted).isEmpty()
    }
}
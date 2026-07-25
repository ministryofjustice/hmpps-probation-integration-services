package uk.gov.justice.digital.hmpps.appointments

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.data.entity.EnforcementRepository
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.UPW_PROJECT_2
import uk.gov.justice.digital.hmpps.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.entity.contact.ContactRepository
import uk.gov.justice.digital.hmpps.entity.contact.ContactType.Code.REVIEW_ENFORCEMENT_STATUS
import uk.gov.justice.digital.hmpps.entity.getStatus
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwDetailsRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.getAppointment
import uk.gov.justice.digital.hmpps.model.Code
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import uk.gov.justice.digital.hmpps.test.TestData
import java.time.DayOfWeek
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
    private val upwDetailsRepository: UpwDetailsRepository,
    private val referenceDataRepository: ReferenceDataRepository,
) {
    companion object {
        val PROJECT = UPW_PROJECT_2.code
    }

    @Test
    fun `404 if appointment id is invalid`() {
        mockMvc
            .put("/projects/$PROJECT/appointments/9999") {
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
            .put("/projects/$PROJECT/appointments/${original.id}") {
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
        val request = TestData.updateAppointment(original.id)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = request
        }.andExpect { status { isOk() } }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment).isNotNull
        assertThat(appointment.date).isEqualTo(request.date)
        assertThat(appointment.startTime).isEqualTo(TestData.startTime)
        assertThat(appointment.endTime).isEqualTo(TestData.endTime)
        assertThat(appointment.penaltyMinutes).isEqualTo(request.penaltyMinutes)
        assertThat(appointment.minutesCredited).isEqualTo(request.minutesCredited)
        assertThat(appointment.project.code).isEqualTo(PROJECT)
        assertThat(appointment.notes).isEqualTo("testing update")
        assertThat(appointment.lastUpdatedDatetime).isCloseTo(appointment.lastUpdatedDatetime, within(1, SECONDS))
        assertThat(appointment.pickUpTime).isEqualTo(LocalTime.of(LocalTime.now().minusHours(1).hour, 30))
        assertThat(appointment.pickUpLocation?.code).isEqualTo(UPWGenerator.DEFAULT_OFFICE_LOCATION.code)
    }

    @Test
    @Transactional
    fun `changing appointment date when outcome exists is rejected`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.DEFAULT_UPW_APPOINTMENT.id)

        mockMvc.put("/projects/${original.project.code}/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(original.rowVersion, original.contact.rowVersion),
                date = original.date.plusDays(1)
            )
        }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Appointment date cannot be changed when an outcome has been recorded")
            }
    }

    @Test
    @Transactional
    fun `non-attendance outcome stores request-supplied UPW fields`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_NO_ENFORCEMENT.id)
        val request = TestData.updateAppointment(original.id).copy(
            version = UUID(original.rowVersion, original.contact.rowVersion),
            startTime = original.startTime,
            endTime = original.endTime,
            outcome = Code("F")
        )

        mockMvc.put("/projects/${original.project.code}/appointments/${original.id}") {
            withToken()
            json = request
        }.andExpect { status { isOk() } }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment.minutesCredited).isEqualTo(request.minutesCredited)
        assertThat(appointment.penaltyMinutes).isEqualTo(request.penaltyMinutes)
        assertThat(appointment.workQuality?.code).isEqualTo(request.workQuality?.code)
        assertThat(appointment.behaviour?.code).isEqualTo(request.behaviour?.code)
        assertThat(appointment.hiVisWorn).isEqualTo(request.hiVisWorn)
        assertThat(appointment.workedIntensively).isEqualTo(request.workedIntensively)
        assertThat(appointment.attended).isFalse
        assertThat(appointment.complied).isFalse
    }

    @Test
    @Transactional
    fun `attended outcome stores request-supplied zero credited minutes`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[0].id)
        val startTime = LocalTime.of(9, 0)
        val endTime = startTime.plusMinutes(65)
        val request = TestData.updateAppointment(original.id).copy(
            version = UUID(original.rowVersion, original.contact.rowVersion),
            startTime = startTime,
            endTime = endTime,
            penaltyMinutes = 65,
            outcome = Code("A")
        )

        mockMvc.put("/projects/${original.project.code}/appointments/${original.id}") {
            withToken()
            json = request
        }.andExpect { status { isOk() } }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment.minutesCredited).isEqualTo(request.minutesCredited)
        assertThat(appointment.penaltyMinutes).isEqualTo(request.penaltyMinutes)
    }

    @Test
    @Transactional
    fun `penalty time greater than duration is stored from request`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[0].id)
        val request = TestData.updateAppointment(original.id).copy(
            version = UUID(original.rowVersion, original.contact.rowVersion),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            penaltyMinutes = 61,
            outcome = Code("A")
        )

        mockMvc.put("/projects/${original.project.code}/appointments/${original.id}") {
            withToken()
            json = request
        }.andExpect { status { isOk() } }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment.penaltyMinutes).isEqualTo(request.penaltyMinutes)
    }

    @Test
    @Transactional
    fun `updated project must be available on appointment date`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[8].id)
        val invalidDate = generateSequence(original.details.disposal.date.plusDays(1)) { it.plusDays(1) }
            .first { it.dayOfWeek != DayOfWeek.MONDAY }

        mockMvc.put("/projects/${original.project.code}/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(original.rowVersion, original.contact.rowVersion),
                date = invalidDate,
                project = Code(UPWGenerator.UPW_PROJECT_2.code)
            )
        }.andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Project is not available")
            }
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

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(outcome = Code("F"))
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

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(outcome = Code("F"))
        }.andExpect { status { is2xxSuccessful() } }

        val enforcement = enforcementRepository.findAll().firstOrNull { it.contact.id == original.contact.id }
        assertThat(enforcement).isNotNull
    }

    @Test
    fun `contact alert created when alertActive is true`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[3].id)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(alertActive = true)
        }.andExpect { status { is2xxSuccessful() } }
        val alert = contactAlertRepository.findAll().firstOrNull { it.contactId == original.contact.id }
        assertThat(alert).isNotNull
    }

    @Test
    fun `contact alert deleted when alertActive is false`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[4].id)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(alertActive = true)
        }.andExpect { status { is2xxSuccessful() } }

        val alertCreated = contactAlertRepository.findAll().filter { it.contactId == original.contact.id }

        val second = unpaidWorkAppointmentRepository.getAppointment(original.id)

        mockMvc.put("/projects/$PROJECT/appointments/${second.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(second.rowVersion, second.contact.rowVersion),
                alertActive = false
            )
        }.andExpect { status { is2xxSuccessful() } }

        val alertDeleted = contactAlertRepository.findAll().filter { it.contactId == original.contact.id }

        assertThat(alertCreated).isNotEmpty
        assertThat(alertDeleted).isEmpty()
    }

    @Test
    @Transactional
    fun `updating appointment with a status code of 'WK' and hours all worked changes to 'HC'`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[5].id)
        upwDetailsRepository.save(original.details.apply { status = referenceDataRepository.getStatus("WK") })
        assertThat(upwDetailsRepository.findByIdOrNull(original.details.id)?.status?.code).isEqualTo("WK")
        val remainingMinutes =
            unpaidWorkAppointmentRepository.getUpwRequiredAndCompletedMinutes(listOf(original.details.id))
                .single()
                .let { it.requiredMinutes + it.positiveAdjustments - it.negativeAdjustments - it.completedMinutes }
        val startTime = LocalTime.of(9, 0)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(original.rowVersion, original.contact.rowVersion),
                startTime = startTime,
                endTime = startTime.plusMinutes(remainingMinutes),
                penaltyMinutes = 0,
                minutesCredited = remainingMinutes
            )
        }.andExpect { status { isOk() } }

        val actualStatus = unpaidWorkAppointmentRepository.getAppointment(original.id).details.status?.code
        assertThat(actualStatus).isEqualTo("HC")
    }

    @Test
    @Transactional
    fun `updating appointment with a status code of 'HC' and hours remaining changes to 'WK'`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[5].id)
        upwDetailsRepository.save(original.details.apply { status = referenceDataRepository.getStatus("HC") })
        assertThat(upwDetailsRepository.findByIdOrNull(original.details.id)?.status?.code).isEqualTo("HC")

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(original.rowVersion, original.contact.rowVersion),
                outcome = Code("F")
            )
        }.andExpect { status { isOk() } }

        val actualStatus = unpaidWorkAppointmentRepository.getAppointment(original.id).details.status?.code
        assertThat(actualStatus).isEqualTo("WK")
    }

    @Test
    fun `updating appointment with a supervisor team sets the team`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[6].id)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(original.rowVersion, original.contact.rowVersion),
                supervisorTeam = Code(TeamGenerator.SECOND_UPW_TEAM.code)
            )
        }.andExpect { status { isOk() } }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment.team.code).isEqualTo(TeamGenerator.SECOND_UPW_TEAM.code)
    }

    @Test
    fun `updating appointment without a supervisor team keeps existing team`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[7].id)
        val originalTeamCode = original.team.code

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(original.rowVersion, original.contact.rowVersion),
                supervisorTeam = null
            )
        }.andExpect { status { isOk() } }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment.team.code).isEqualTo(originalTeamCode)
    }

    @Test
    fun `updating appointment with a different project sets the project`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[8].id)
        val newProject = UPWGenerator.UPW_PROJECT_1

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(original.rowVersion, original.contact.rowVersion),
                project = Code(newProject.code)
            )
        }.andExpect { status { isOk() } }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment.project.code).isEqualTo(newProject.code)
    }

    @Test
    fun `updating appointment without a project keeps existing project`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[9].id)

        mockMvc.put("/projects/$PROJECT/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(original.rowVersion, original.contact.rowVersion),
                project = null
            )
        }.andExpect { status { isOk() } }

        val appointment = unpaidWorkAppointmentRepository.getAppointment(original.id)
        assertThat(appointment.project.code).isEqualTo(PROJECT)
    }

    @Test
    fun `updating appointment with an invalid project code returns an error`() {
        val original = unpaidWorkAppointmentRepository.getAppointment(UPWGenerator.UPW_APPOINTMENT_TO_UPDATE[8].id)

        mockMvc.put("/projects/${original.project.code}/appointments/${original.id}") {
            withToken()
            json = TestData.updateAppointment(original.id).copy(
                version = UUID(original.rowVersion, original.contact.rowVersion),
                project = Code("INVALID")
            )
        }.andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("UnpaidWorkProject with code of INVALID not found")
            }
    }
}

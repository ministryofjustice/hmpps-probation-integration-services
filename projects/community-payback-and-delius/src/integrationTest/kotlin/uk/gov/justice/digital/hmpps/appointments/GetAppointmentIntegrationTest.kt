package uk.gov.justice.digital.hmpps.appointments

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.UPW_PROJECT_1
import uk.gov.justice.digital.hmpps.entity.contact.ContactRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class GetAppointmentIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val contactRepository: ContactRepository,
) {
    companion object {
        val PROJECT = UPW_PROJECT_1.code
    }

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
        val versions = UUID(
            unpaidWorkAppointmentRepository.findByIdOrNull(UPWGenerator.DEFAULT_UPW_APPOINTMENT.id)!!.rowVersion,
            contactRepository.findByIdOrNull(UPWGenerator.DEFAULT_CONTACT.id)!!.rowVersion
        )

        val response =
            mockMvc.get("/projects/$PROJECT/appointments/${UPWGenerator.DEFAULT_UPW_APPOINTMENT.id}?username=DefaultUser") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<AppointmentResponse>()

        assertThat(response.version).isEqualTo(versions)
        assertThat(response.project.name).isEqualTo("Default UPW Project")
        assertThat(response.case.crn).isEqualTo(PersonGenerator.DEFAULT_PERSON.crn)
        assertThat(response.event.number).isEqualTo(1)
        assertThat(response.penaltyHours).isEqualTo("01:05")
        assertThat(response.enforcementAction!!.respondBy).isEqualTo(response.date.plusDays(ReferenceDataGenerator.ROM_ENFORCEMENT_ACTION.responseByPeriod!!))
        assertThat(response.behaviour).isEqualTo(Behaviour.EXCELLENT)
        assertThat(response.pickUpData!!.locationCode).isEqualTo(Code(UPWGenerator.DEFAULT_UPW_APPOINTMENT.pickUpLocation!!.code))
        assertThat(response.workQuality).isEqualTo(WorkQuality.EXCELLENT)
    }

    @Test
    fun `can retrieve single session details`() {
        val response = mockMvc
            .get("/projects/$PROJECT/appointments?date=${LocalDate.now().plusDays(1)}&username=DefaultUser") {
                withToken()
            }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<SessionResponse>()
        assertThat(response.project.name).isEqualTo("Default UPW Project")
        assertThat(response.appointmentSummaries.size).isEqualTo(1)
        assertThat(response.appointmentSummaries[0].case.crn).isEqualTo("Z000001")
        assertThat(response.appointmentSummaries[0].requirementProgress.requiredMinutes).isEqualTo(120 * 60)
        assertThat(response.appointmentSummaries[0].requirementProgress.adjustments).isEqualTo(4)
    }

    @Test
    fun `returns 2xx when limited access check passes but with current restriction flag true`() {
        val appointmentId = UPWGenerator.LAO_RESTRICTED_UPW_APPOINTMENT.id
        val response =
            mockMvc.get("/projects/$PROJECT/appointments/$appointmentId?username=LimitedAccess") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<AppointmentResponse>()
        assertThat(response.case.currentRestriction).isEqualTo(true)
        assertThat(response.case.restrictionMessage).isNotNull()
    }

    @Test
    fun `returns 2xx when limited access check fails but with current restriction flag true`() {
        val appointmentId = UPWGenerator.LAO_RESTRICTED_UPW_APPOINTMENT.id
        val response =
            mockMvc.get("/projects/$PROJECT/appointments/$appointmentId?username=FullAccess") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<AppointmentResponse>()
        assertThat(response.case.currentRestriction).isEqualTo(false)
        assertThat(response.case.restrictionMessage.isNullOrEmpty())
    }

    @Test
    fun `returns 2xx when limited access check fails but with current exclusion flag true`() {
        val appointmentId = UPWGenerator.LAO_EXCLUDED_UPW_APPOINTMENT.id

        val response =
            mockMvc.get("/projects/$PROJECT/appointments/$appointmentId?username=LimitedAccess") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<AppointmentResponse>()

        assertThat(response.case.currentExclusion).isEqualTo(true)
        assertThat(response.case.exclusionMessage).isNotNull
    }
}
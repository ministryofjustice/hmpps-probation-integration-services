package uk.gov.justice.digital.hmpps.appointments

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.web.PagedModel.PageMetadata
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.UPW_PROJECT_1
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
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

    private data class PagedModel<T>(
        val content: List<T>,
        val page: PageMetadata
    )

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
        assertThat(response.pickUpData!!.location!!.description).isEqualTo(UPWGenerator.DEFAULT_UPW_APPOINTMENT.pickUpLocation!!.description)
        assertThat(response.pickUpData!!.location!!.code).isEqualTo(UPWGenerator.DEFAULT_UPW_APPOINTMENT.pickUpLocation!!.code)
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

    @Test
    fun `can retrieve appointment details with null pickup location`() {
        val appointmentId = UPWGenerator.UPW_APPOINTMENT_WITHOUT_PICKUP.id

        val response =
            mockMvc.get("/projects/$PROJECT/appointments/$appointmentId?username=DefaultUser") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsJson<AppointmentResponse>()

        assertThat(response.pickUpData?.location).isNull()
    }

    @Test
    fun `can retrieve all appointments for a crn without sort`() {
        mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&crn=${PersonGenerator.DEFAULT_PERSON.crn}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                content { jsonPath("$.content.size()") { value(10) } }
            }
    }

    @Test
    fun `can retrieve all appointments without filters`() {
        mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                content { jsonPath("$.content.size()") { value(10) } }
            }
    }

    @Test
    fun `can retrieve all appointments with a date filter from`() {
        val response = mockMvc
            .get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&fromDate=${LocalDate.now()}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content)
            .isNotEmpty
            .allSatisfy { assertThat(it.date).isAfterOrEqualTo(LocalDate.now()) }
    }

    @Test
    fun `can retrieve all appointments with a date filter to`() {
        mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&toDate=${LocalDate.now()}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                content { jsonPath("$.content.size()") { value(10) } }
            }
    }

    @Test
    fun `can retrieve all appointments by crn and date sort desc`() {
        val response = mockMvc
            .get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&crn=${PersonGenerator.DEFAULT_PERSON.crn}&sort=date,desc") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).hasSize(10)
        assertThat(response.content.first().id).isEqualTo(UPWGenerator.DEFAULT_UPW_APPOINTMENT.id)
        assertThat(response.content.map { it.case.crn }).containsOnly(PersonGenerator.DEFAULT_PERSON.crn)
        assertThat(response.content.map { it.date }).isSortedAccordingTo(Comparator.reverseOrder())
    }

    @Test
    fun `can retrieve appointments without outcome code`() {
        mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&outcomeCodes=NO_OUTCOME") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    jsonPath("$.content.size()") { value(10) }
                    jsonPath("$.content[0].outcome") { doesNotExist() }
                    jsonPath("$.content[1].outcome") { doesNotExist() }
                    jsonPath("$.content[2].outcome") { doesNotExist() }
                    jsonPath("$.content[3].outcome") { doesNotExist() }
                    jsonPath("$.content[4].outcome") { doesNotExist() }
                    jsonPath("$.content[5].outcome") { doesNotExist() }
                    jsonPath("$.content[6].outcome") { doesNotExist() }
                    jsonPath("$.content[7].outcome") { doesNotExist() }
                    jsonPath("$.content[8].outcome") { doesNotExist() }
                    jsonPath("$.content[9].outcome") { doesNotExist() }
                }
            }
    }

    @Test
    fun `can retrieve appointments with outcome code`() {
        val outcomeCode = "F"
        val response =
            mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&outcomeCodes=$outcomeCode") { withToken() }
                .andExpect {
                    status { is2xxSuccessful() }
                }
                .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty
        assertThat(response.content.mapNotNull { it.outcome?.code }.distinct()).containsOnly(outcomeCode)
    }

    @Test
    fun `can retrieve appointments with outcome codes and also NO_OUTCOME`() {
        val outcomeCode = "F"
        val noOutcomeCode = "NO_OUTCOME"
        val outcomeOnlyResponse = mockMvc
            .get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&outcomeCodes=$outcomeCode") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()
        assertThat(outcomeOnlyResponse.content).isNotEmpty.allSatisfy { assertThat(it.outcome?.code).isEqualTo("F") }

        val response = mockMvc
            .get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&outcomeCodes=$outcomeCode&outcomeCodes=$noOutcomeCode") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()
        assertThat(outcomeOnlyResponse.content).isNotEmpty.allSatisfy { assertThat(it.outcome?.code).isIn("F", null) }

        assertThat(response.page.totalElements).isGreaterThan(outcomeOnlyResponse.page.totalElements)
    }
}

package uk.gov.justice.digital.hmpps.appointments

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import tools.jackson.databind.ObjectMapper
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
import kotlin.collections.map

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

    private val objectMapper = ObjectMapper()

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
        val response = mockMvc.get("/appointments?crn=${PersonGenerator.DEFAULT_PERSON.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsString
        val node = objectMapper.readTree(response)
        val ids = node["content"].map { it["id"].asLong() }
        assertThat(ids).size().isEqualTo(5)
    }

    @Test
    fun `can retrieve all appointments without filters`() {
        val response = mockMvc.get("/appointments") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsString
        val node = objectMapper.readTree(response)
        val ids = node["content"].map { it["id"].asLong() }
        assertThat(ids).size().isEqualTo(8)
    }

    @Test
    fun `can retrieve all appointments with a date filter from`() {
        val responseString = mockMvc.get("/appointments?fromDate=${LocalDate.now()}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsString
        println(responseString)
        val node = objectMapper.readTree(responseString)
        val ids = node["content"].map { it["id"].asLong() }
        assertThat(ids).size().isEqualTo(4)
    }

    @Test
    fun `can retrieve all appointments with a date filter to`() {
        val responseString = mockMvc.get("/appointments?toDate=${LocalDate.now()}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsString
        println(responseString)
        val node = objectMapper.readTree(responseString)
        val ids = node["content"].map { it["id"].asLong() }
        assertThat(ids).size().isEqualTo(6)
    }

    @Test
    fun `can retrieve all appointments by crn and date sort desc`() {
        val responseString =
            mockMvc.get("/appointments?crn=${PersonGenerator.DEFAULT_PERSON.crn}&sort=date,desc") { withToken() }
                .andExpect { status { is2xxSuccessful() } }
                .andReturn().response.contentAsString
        val node = objectMapper.readTree(responseString)
        val ids = node["content"].map { it["id"].asLong() }
        assertThat(ids).containsExactly(
            UPWGenerator.DEFAULT_UPW_APPOINTMENT.id,
            UPWGenerator.OVERDUE_APPOINTMENT.id, 1L, 2L, 3L
        )
    }
}
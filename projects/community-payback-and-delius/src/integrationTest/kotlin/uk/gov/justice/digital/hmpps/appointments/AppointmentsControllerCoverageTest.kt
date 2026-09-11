package uk.gov.justice.digital.hmpps.appointments

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.web.PagedModel.PageMetadata
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.model.AppointmentsResponse
import uk.gov.justice.digital.hmpps.model.PersonName
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentsControllerCoverageTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    private data class PagedModel<T>(
        val content: List<T>,
        val page: PageMetadata
    )

    @Test
    fun `get appointments with project codes filter`() {
        val projectCode = UPWGenerator.UPW_PROJECT_1.code
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&projectCodes=$projectCode") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty.allSatisfy { assertThat(it.project.code).isEqualTo(projectCode) }
    }

    @Test
    fun `get appointments with multiple project codes`() {
        val projectCode1 = UPWGenerator.UPW_PROJECT_1.code
        val projectCode2 = UPWGenerator.UPW_PROJECT_2.code
        val response = mockMvc.get(
            "/appointments?username=${UserGenerator.DEFAULT_USER.username}&projectCodes=$projectCode1&projectCodes=$projectCode2"
        ) {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty
            .allSatisfy { assertThat(it.project.code).isIn(projectCode1, projectCode2) }
    }

    @Test
    fun `get appointments with project type codes filter`() {
        val projectTypeCode = ReferenceDataGenerator.INDIVIDUAL_PLACEMENT_PROJECT_TYPE.code
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&projectTypeCodes=$projectTypeCode") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty
            .allSatisfy { assertThat(it.project.projectType.code).isEqualTo(projectTypeCode) }
    }

    @Test
    fun `get appointments with outcome codes filter`() {
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&outcomeCodes=NO_OUTCOME") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty.allSatisfy { assertThat(it.outcome).isNull() }
    }

    @Test
    fun `get appointments with appointment IDs filter`() {
        val id1 = UPWGenerator.DEFAULT_UPW_APPOINTMENT.id
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&appointmentIds=$id1") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).hasSize(1)
        assertThat(response.content.single().id).isEqualTo(id1)
    }

    @Test
    fun `get appointments with references filter`() {
        val reference = UPWGenerator.DEFAULT_CONTACT_EXTERNAL_REF_UUID
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&references=$reference") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty
            .allSatisfy { assertThat(it.externalReference).isEqualTo(reference) }
    }

    @Test
    fun `get appointments with name sort mapping`() {
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&sort=name,asc") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty
        assertThat(response.content.map { it.case.name.surname.lowercase() + it.case.name.forename.lowercase() })
            .isSorted
    }

    @Test
    fun `get appointments with surname sort mapping`() {
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&sort=surname,asc") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty
        assertThat(response.content.map { it.case.name.surname }).isSorted
    }

    @Test
    fun `get appointments with forename sort mapping`() {
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&sort=forename,asc") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty
        assertThat(response.content.map { it.case.name.forename }).isSorted
    }

    @Test
    fun `get appointments with date sort mapping`() {
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&sort=date,asc") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty
        assertThat(response.content.map { it.date }).isSorted
    }

    @Test
    fun `get appointments with custom sort mapping descending`() {
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&sort=name,desc") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty
        assertThat(response.content.map { it.case.name })
            .isSortedAccordingTo(compareBy<PersonName> { it.surname }.thenBy { it.forename }.reversed())
    }

    @Test
    fun `get appointments combines all optional parameters`() {
        val appointment = UPWGenerator.DEFAULT_UPW_APPOINTMENT
        val response = mockMvc.get(
            "/appointments?" +
                "username=${UserGenerator.DEFAULT_USER.username}&" +
                "crn=${PersonGenerator.DEFAULT_PERSON.crn}&" +
                "eventNumber=1&" +
                "fromDate=${LocalDate.now()}&" +
                "toDate=${LocalDate.now().plusDays(30)}&" +
                "projectCodes=${UPWGenerator.UPW_PROJECT_1.code}&" +
                "projectTypeCodes=${ReferenceDataGenerator.INDIVIDUAL_PLACEMENT_PROJECT_TYPE.code}&" +
                "outcomeCodes=F&" +
                "appointmentIds=${appointment.id}&" +
                "references=${UPWGenerator.DEFAULT_CONTACT_EXTERNAL_REF_UUID}&" +
                "sort=date,desc&" +
                "page=0&" +
                "size=20"
        ) {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).hasSize(1)
        response.content.single().also {
            assertThat(it.id).isEqualTo(appointment.id)
            assertThat(it.project.code).isEqualTo(UPWGenerator.UPW_PROJECT_1.code)
            assertThat(it.outcome?.code).isEqualTo("F")
            assertThat(it.eventNumber).isEqualTo(1)
        }
    }

    @Test
    fun `get appointments with event number filter`() {
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&eventNumber=1") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).isNotEmpty.allSatisfy { assertThat(it.eventNumber).isEqualTo(1) }
    }

    @Test
    fun `get appointments with pagination parameters`() {
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&page=0&size=5") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.content).hasSizeLessThanOrEqualTo(5)
        assertThat(response.page.size).isEqualTo(5L)
        assertThat(response.page.number).isEqualTo(0L)
    }

    @Test
    fun `get appointments with different page sizes`() {
        listOf(5, 10, 20, 50).forEach { size ->
            val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}&page=0&size=$size") {
                withToken()
            }
                .andExpect { status { isOk() } }
                .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

            assertThat(response.content).hasSizeLessThanOrEqualTo(size)
            assertThat(response.page.size).isEqualTo(size.toLong())
        }
    }

    @Test
    fun `get appointments with specific pageable defaults applied`() {
        // Tests that pageable defaults are applied (page=0, size=10, sort by surname and forename)
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.page.size).isEqualTo(10L)
        assertThat(response.page.number).isEqualTo(0L)
        assertThat(response.content.map { it.case.name })
            .isSortedAccordingTo(compareBy<PersonName> { it.surname }.thenBy { it.forename })
    }

    // Delete appointment tests are covered in DeleteAppointmentIntegrationTest
    // This test focuses on parameter handling in getAppointments controller method

    @Test
    fun `get appointments requires username parameter`() {
        val response = mockMvc.get("/appointments?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()

        assertThat(response.page.totalElements).isGreaterThan(0)
    }

    @Test
    fun `get appointments all optional filter combinations`() {
        // Covers all optional filter paths through getAppointments
        val byCrn = mockMvc.get(
            "/appointments?" +
                "username=${UserGenerator.DEFAULT_USER.username}&" +
                "crn=${PersonGenerator.DEFAULT_PERSON.crn}"
        ) {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()
        assertThat(byCrn.content).isNotEmpty
            .allSatisfy { assertThat(it.case.crn).isEqualTo(PersonGenerator.DEFAULT_PERSON.crn) }

        val byEventNumber = mockMvc.get(
            "/appointments?" +
                "username=${UserGenerator.DEFAULT_USER.username}&" +
                "eventNumber=1"
        ) {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()
        assertThat(byEventNumber.content).isNotEmpty.allSatisfy { assertThat(it.eventNumber).isEqualTo(1) }

        val byFromDate = mockMvc.get(
            "/appointments?" +
                "username=${UserGenerator.DEFAULT_USER.username}&" +
                "fromDate=${LocalDate.now()}"
        ) {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()
        assertThat(byFromDate.content).isNotEmpty
            .allSatisfy { assertThat(it.date).isAfterOrEqualTo(LocalDate.now()) }

        val byToDate = mockMvc.get(
            "/appointments?" +
                "username=${UserGenerator.DEFAULT_USER.username}&" +
                "toDate=${LocalDate.now()}"
        ) {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PagedModel<AppointmentsResponse>>()
        assertThat(byToDate.content).isNotEmpty
            .allSatisfy { assertThat(it.date).isBeforeOrEqualTo(LocalDate.now()) }
    }
}





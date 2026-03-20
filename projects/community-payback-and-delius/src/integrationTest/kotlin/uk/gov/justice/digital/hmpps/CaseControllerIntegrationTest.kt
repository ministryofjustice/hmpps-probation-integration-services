package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.model.ScheduleResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
class CaseControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `returns schedule with requirement progress, allocations and appointments`() {
        val response = mockMvc
            .get("/case/${PersonGenerator.DEFAULT_PERSON.crn}/event/${UPWGenerator.EVENT_1.number}/appointments/schedule") {
                withToken()
            }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<ScheduleResponse>()

        assertThat(response.requirementProgress).isNotNull
        assertThat(response.requirementProgress.requiredMinutes).isEqualTo(7200) // rqmnt length of 120 hours * 60
        assertThat(response.requirementProgress.completedMinutes).isEqualTo(870) // sum of minutesCredited on appointments

        assertThat(response.allocations).isNotEmpty
        val allocation = response.allocations.first()
        assertThat(allocation.id).isEqualTo(UPWGenerator.DEFAULT_UPW_ALLOCATION.id)
        assertThat(allocation.pickUp!!.time).isEqualTo(UPWGenerator.DEFAULT_UPW_ALLOCATION.pickUpTime)
        assertThat(allocation.pickUp!!.location!!.code).isEqualTo(UPWGenerator.DEFAULT_OFFICE_LOCATION.code)
        assertThat(allocation.pickUp!!.location!!.description).isEqualTo(UPWGenerator.DEFAULT_OFFICE_LOCATION.description)
        assertThat(allocation.project.name).isEqualTo("Default UPW Project")
        assertThat(allocation.project.code).isEqualTo("N01P01")
        assertThat(allocation.project.expectedEndDateExclusive).isEqualTo(UPWGenerator.UPW_PROJECT_1.expectedEndDate)
        assertThat(allocation.dayOfWeek).isEqualTo("MONDAY")
        assertThat(allocation.frequency).isEqualTo("Weekly")
        assertThat(allocation.startTime.toString()).isEqualTo("09:00")
        assertThat(allocation.endTime.toString()).isEqualTo("16:00")

        assertThat(response.appointments).isNotEmpty
    }

    @Test
    fun `returns 404 when CRN does not exist`() {
        mockMvc.get("/case/X999999/event/1/appointments/schedule") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Person with crn of X999999 not found")
            }
    }

    @Test
    fun `returns 404 when event number does not exist for person`() {
        mockMvc.get("/case/${PersonGenerator.DEFAULT_PERSON.crn}/event/999/appointments/schedule") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Event with event number of 999 not found")
            }
    }

    @Test
    fun `returns 200 when summary requested`() {
        val username = UserGenerator.DEFAULT_USER.username
        val response =
            mockMvc.get("/case/${PersonGenerator.DEFAULT_PERSON.crn}/summary?username=${username}") { withToken() }
                .andExpect { status { isOk() } }
                .andReturn().response.contentAsJson<Map<String, Any>>()

        val unpaidWorkDetails = response["unpaidWorkDetails"] as? List<Map<String, Any>>
        assertThat(unpaidWorkDetails).isNotNull
        assertThat(unpaidWorkDetails).hasSize(5)

        unpaidWorkDetails!!.forEachIndexed { idx, detail ->
            assertThat(detail["eventNumber"]).isEqualTo(idx + 1)
            assertThat(detail["requiredMinutes"]).isInstanceOf(Number::class.java)
            assertThat(detail["completedMinutes"]).isInstanceOf(Number::class.java)
            assertThat(detail["completedEteMinutes"]).isInstanceOf(Number::class.java)
            // Only check that 'adjustments' is present and is a number
            assertThat(detail).containsKey("adjustments")
            assertThat(detail["adjustments"]).isInstanceOf(Number::class.java)
        }
    }

    @Test
    fun `returns 200 with restricted and excluded case summary if username not provided`() {
        mockMvc.get("/case/${PersonGenerator.DEFAULT_PERSON.crn}/summary") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.case.currentExclusion") { value(true) }
                jsonPath("$.case.exclusionMessage") { value("username not provided so cannot determine exclusion") }
                jsonPath("$.case.currentRestriction") { value(true) }
                jsonPath("$.case.restrictionMessage") { value("username not provided so cannot determine restriction") }
            }
    }

    @Test
    fun `returns 404 for unknown person`() {
        mockMvc.get("/case/X999999/summary?username=${UserGenerator.DEFAULT_USER.username}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Person with crn of X999999 not found")
            }
    }
}

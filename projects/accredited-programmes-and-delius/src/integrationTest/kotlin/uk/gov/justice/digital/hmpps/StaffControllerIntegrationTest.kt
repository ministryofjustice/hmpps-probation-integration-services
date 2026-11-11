package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StaffControllerIntegrationTest(@Autowired private val mockMvc: MockMvc) {
    @Test
    fun `returns staff names for a given staff code`() {
        val staff = TestData.AP_STAFF

        mockMvc.get("/staff/${staff.code}") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("forename") { value(staff.forename) }
                jsonPath("surname") { value(staff.surname) }
                jsonPath("middleNames") { value(staff.middleName) }
            }
    }

    @Test
    fun `returns 404 not found for unknown staff code`() {
        mockMvc.get("/staff/ZZZZZZZ") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `unauthorized status returned when no token provided`() {
        mockMvc.get("/staff/${TestData.AP_STAFF.code}")
            .andExpect { status { isUnauthorized() } }
    }
}

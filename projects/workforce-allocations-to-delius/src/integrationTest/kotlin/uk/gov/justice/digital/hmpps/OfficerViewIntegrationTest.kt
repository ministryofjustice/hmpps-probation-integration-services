package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OfficerViewIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `successful response`() {
        val staff = StaffGenerator.DEFAULT
        mockMvc.get("/staff/${StaffGenerator.DEFAULT.code}/officer-view") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.code") { value(staff.code) }
                jsonPath("$.name.forename") { value(staff.forename) }
                jsonPath("$.name.surname") { value(staff.surname) }
                jsonPath("$.casesDueToEndInNext4Weeks") { value(1) }
                jsonPath("$.releasesWithinNext4Weeks") { value(1) }
                jsonPath("$.paroleReportsToCompleteInNext4Weeks") { value(1) }
            }
    }
}

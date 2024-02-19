package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OfficerViewIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `successful response`() {
        val staff = StaffGenerator.DEFAULT
        mockMvc.perform(get("/staff/${StaffGenerator.DEFAULT.code}/officer-view").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.code").value(staff.code))
            .andExpect(jsonPath("$.name.forename").value(staff.forename))
            .andExpect(jsonPath("$.name.surname").value(staff.surname))
            .andExpect(jsonPath("$.casesDueToEndInNext4Weeks").value(1))
            .andExpect(jsonPath("$.releasesWithinNext4Weeks").value(1))
            .andExpect(jsonPath("$.paroleReportsToCompleteInNext4Weeks").value(1))
    }
}

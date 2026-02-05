package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProviderIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `providers are returned successfully`() {
        mockMvc
            .get("/providers") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("length()") { equalTo(1) }
                jsonPath("[0].code") { equalTo("TST") }
                jsonPath("[0].description") { equalTo("Test") }
            }
    }

    @Test
    fun `single provider is returned successfully`() {
        mockMvc
            .get("/providers/TST") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("code") { equalTo("TST") }
                jsonPath("description") { equalTo("Test") }
                jsonPath("localAdminUnits.length()") { equalTo(1) }
                jsonPath("localAdminUnits[0].code") { equalTo("LAU") }
                jsonPath("localAdminUnits[0].description") { equalTo("Local Admin Unit") }
            }
    }

    @Test
    fun `non-existent provider returns 404`() {
        mockMvc
            .get("/providers/DOESNOTEXIST") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `local admin unit is returned successfully`() {
        mockMvc
            .get("/providers/TST/localAdminUnits/LAU") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("code") { equalTo("LAU") }
                jsonPath("description") { equalTo("Local Admin Unit") }
                jsonPath("teams.length()") { equalTo(2) }
                jsonPath("teams[0].code") { equalTo("TEAM01") }
                jsonPath("teams[0].description") { equalTo("Team 1") }
                jsonPath("teams[1].code") { equalTo("TEAM02") }
                jsonPath("teams[1].description") { equalTo("Team 2") }
            }
    }

    @Test
    fun `non-existent local admin unit returns 404`() {
        mockMvc
            .get("/providers/TST/localAdminUnits/DOESNOTEXIST") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}

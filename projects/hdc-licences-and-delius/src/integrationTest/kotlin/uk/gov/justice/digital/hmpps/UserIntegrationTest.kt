package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `user details are returned successfully`() {
        mockMvc.get("/users/test.user/details") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("username") { value(equalTo("test.user")) }
                jsonPath("enabled") { value(equalTo(true)) }
                jsonPath("roles.length()") { value(equalTo(1)) }
                jsonPath("roles[0]") { value(equalTo("LHDCBT002")) }
            }
    }

    @Test
    fun `user with end date is returned with enabled=false`() {
        mockMvc.get("/users/inactive.user/details") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("username") { value(equalTo("inactive.user")) }
                jsonPath("enabled") { value(equalTo(false)) }
            }
    }

    @Test
    fun `invalid role is rejected`() {
        mockMvc.put("/users/test.user/roles/INVALID") { withToken() }
            .andExpect { status { isBadRequest() } }
        mockMvc.delete("/users/test.user/roles/INVALID") { withToken() }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `role can be added and removed`() {
        mockMvc.put("/users/test.user/roles/LHDCBT003") { withToken() }
            .andExpect { status { isOk() } }
        mockMvc.get("/users/test.user/details") { withToken() }
            .andExpect { jsonPath("roles") { value(hasItems("LHDCBT002", "LHDCBT003")) } }
        mockMvc.delete("/users/test.user/roles/LHDCBT003") { withToken() }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `non existent user returns not found`() {
        mockMvc.put("/users/nonexistent.user/roles/LHDCBT003") { withToken() }
            .andExpect { status { isNotFound() } }
        mockMvc.delete("/users/nonexistent.user/roles/LHDCBT003") { withToken() }
            .andExpect { status { isOk() } }
        mockMvc.get("/users/nonexistent.user/details") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}

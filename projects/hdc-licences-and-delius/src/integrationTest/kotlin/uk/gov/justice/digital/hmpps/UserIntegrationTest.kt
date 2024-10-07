package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `user details are returned successfully`() {
        mockMvc
            .perform(get("/users/test.user/details").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("username", equalTo("test.user")))
            .andExpect(jsonPath("enabled", equalTo(true)))
            .andExpect(jsonPath("roles.length()", equalTo(1)))
            .andExpect(jsonPath("roles[0]", equalTo("LHDCBT002")))
    }

    @Test
    fun `user with end date is returned with enabled=false`() {
        mockMvc
            .perform(get("/users/inactive.user/details").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("username", equalTo("inactive.user")))
            .andExpect(jsonPath("enabled", equalTo(false)))
    }

    @Test
    fun `invalid role is rejected`() {
        mockMvc
            .perform(put("/users/test.user/roles/INVALID").withToken())
            .andExpect(status().isBadRequest)
        mockMvc
            .perform(delete("/users/test.user/roles/INVALID").withToken())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `role can be added and removed`() {
        mockMvc.perform(put("/users/test.user/roles/LHDCBT003").withToken())
            .andExpect(status().isOk)
        mockMvc
            .perform(get("/users/test.user/details").withToken())
            .andExpect(jsonPath("roles", hasItems("LHDCBT002", "LHDCBT003")))
        mockMvc.perform(delete("/users/test.user/roles/LHDCBT003").withToken())
            .andExpect(status().isOk)
    }

    @Test
    fun `non existent user returns not found`() {
        mockMvc.perform(put("/users/nonexistent.user/roles/LHDCBT003").withToken())
            .andExpect(status().isNotFound)
        mockMvc
            .perform(delete("/users/nonexistent.user/roles/LHDCBT003").withToken())
            .andExpect(status().isOk)
        mockMvc
            .perform(get("/users/nonexistent.user/details").withToken())
            .andExpect(status().isNotFound)
    }
}

package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `user details are returned successfully`() {
        mockMvc
            .perform(get("/users/test.user/details").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("username", equalTo("test.user")))
            .andExpect(jsonPath("enabled", equalTo(true)))
            .andExpect(jsonPath("roles.length()", equalTo(1)))
            .andExpect(jsonPath("roles[0]", equalTo("LHDCBT002")))
    }

    @Test
    fun `user with end date is returned with enabled=false`() {
        mockMvc
            .perform(get("/users/inactive.user/details").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("username", equalTo("inactive.user")))
            .andExpect(jsonPath("enabled", equalTo(false)))
    }

    @Test
    fun `invalid role is rejected`() {
        mockMvc
            .perform(put("/users/test.user/roles/INVALID").withOAuth2Token(wireMockServer))
            .andExpect(status().isBadRequest)
        mockMvc
            .perform(delete("/users/test.user/roles/INVALID").withOAuth2Token(wireMockServer))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `role can be added and removed`() {
        mockMvc.perform(put("/users/test.user/roles/LHDCBT003").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
        mockMvc
            .perform(get("/users/test.user/details").withOAuth2Token(wireMockServer))
            .andExpect(jsonPath("roles", hasItems("LHDCBT002", "LHDCBT003")))
        mockMvc.perform(delete("/users/test.user/roles/LHDCBT003").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
    }
}

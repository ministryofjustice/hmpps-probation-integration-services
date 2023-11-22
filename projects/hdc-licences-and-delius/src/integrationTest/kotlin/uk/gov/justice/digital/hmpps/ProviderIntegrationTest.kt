package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProviderIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `providers are returned successfully`() {
        mockMvc
            .perform(get("/providers").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("length()", equalTo(1)))
            .andExpect(jsonPath("[0].code", equalTo("TST")))
            .andExpect(jsonPath("[0].description", equalTo("Test")))
    }

    @Test
    fun `single provider is returned successfully`() {
        mockMvc
            .perform(get("/providers/TST").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("code", equalTo("TST")))
            .andExpect(jsonPath("description", equalTo("Test")))
            .andExpect(jsonPath("localAdminUnits.length()", equalTo(1)))
            .andExpect(jsonPath("localAdminUnits[0].code", equalTo("LAU")))
            .andExpect(jsonPath("localAdminUnits[0].description", equalTo("Local Admin Unit")))
    }

    @Test
    fun `non-existent provider returns 404`() {
        mockMvc
            .perform(get("/providers/DOESNOTEXIST").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `local admin unit is returned successfully`() {
        mockMvc
            .perform(get("/providers/TST/localAdminUnits/LAU").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("code", equalTo("LAU")))
            .andExpect(jsonPath("description", equalTo("Local Admin Unit")))
            .andExpect(jsonPath("teams.length()", equalTo(2)))
            .andExpect(jsonPath("teams[0].code", equalTo("TEAM01")))
            .andExpect(jsonPath("teams[0].description", equalTo("Team 1")))
            .andExpect(jsonPath("teams[1].code", equalTo("TEAM02")))
            .andExpect(jsonPath("teams[1].description", equalTo("Team 2")))
    }

    @Test
    fun `non-existent local admin unit returns 404`() {
        mockMvc
            .perform(get("/providers/TST/localAdminUnits/DOESNOTEXIST").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }
}

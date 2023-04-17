package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
internal class StaffIntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var wireMockserver: WireMockServer

    @Test
    fun `get staff code`() {
        val username = "WithStaff"
        mockMvc.perform(get("/user/$username/staff").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.code", equalTo("TEST001")))
    }

    @Test
    fun `no staff code`() {
        val username = "WithoutStaff"
        mockMvc.perform(get("/user/$username/staff").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.code").doesNotExist())
    }
}

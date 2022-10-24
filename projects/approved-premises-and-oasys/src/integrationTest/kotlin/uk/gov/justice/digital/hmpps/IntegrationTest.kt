package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `get latest layer 3 assessment`() {
        mockMvc
            .perform(get("/latest-assessment/D006296").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `get latest layer 3 assessment not found`() {
        mockMvc
            .perform(get("/latest-assessment/D000000").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }
}

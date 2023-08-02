package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var wireMockServer: WireMockServer

    @MockBean lateinit var telemetryService: TelemetryService

    @Test
    fun `API call retuns a success response`() {
        mockMvc
            .perform(get("/duty-to-refer-nsi/123?type=CRN").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `API call retuns a 404 response`() {
        mockMvc
            .perform(get("/duty-to-refer-nsi/N123123B?type=CRN").withOAuth2Token(wireMockServer))
            .andExpect(status().is4xxClientError)
    }
}

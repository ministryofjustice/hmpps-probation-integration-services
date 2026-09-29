package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `API call returns a success response`() {
        mockMvc.get("/regions") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
    }
}

package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.ManagedStatus
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @ParameterizedTest
    @MethodSource("probationStatuses")
    fun `correct status returned for each case`(crn: String, status: ManagedStatus) {
        mockMvc
            .perform(get("/probation-case/$crn/status").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(status.name))
    }

    companion object {
        @JvmStatic
        fun probationStatuses() = listOf(
            Arguments.of(PersonGenerator.NEW_TO_PROBATION.crn, ManagedStatus.NEW_TO_PROBATION),
            Arguments.of(PersonGenerator.CURRENTLY_MANAGED.crn, ManagedStatus.CURRENTLY_MANAGED),
            Arguments.of(PersonGenerator.PREVIOUSLY_MANAGED.crn, ManagedStatus.PREVIOUSLY_MANAGED),
            Arguments.of(PersonGenerator.NO_SENTENCE.crn, ManagedStatus.UNKNOWN)
        )
    }
}

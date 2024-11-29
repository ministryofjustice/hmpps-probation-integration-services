package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.ProbationStatus
import uk.gov.justice.digital.hmpps.api.model.ProbationStatusDetail
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @ParameterizedTest
    @MethodSource("probationStatuses")
    fun `correct status returned for each case`(crn: String, statusDetail: ProbationStatusDetail) {
        val expect = mockMvc
            .perform(get("/probation-case/$crn/status").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.status").value(statusDetail.status.name))
            .andExpect(jsonPath("$.inBreach").value(statusDetail.inBreach))
            .andExpect(jsonPath("$.preSentenceActivity").value(statusDetail.preSentenceActivity))
            .andExpect(jsonPath("$.awaitingPsr").value(statusDetail.awaitingPsr))

        statusDetail.terminationDate?.let {
            expect.andExpect(jsonPath("$.terminationDate").value(it.toString()))
        }
    }

    companion object {

        private val DEFAULT_DETAIL = ProbationStatusDetail.NO_RECORD

        @JvmStatic
        fun probationStatuses() = listOf(
            Arguments.of("D035N73", DEFAULT_DETAIL),
            Arguments.of(PersonGenerator.NEW_TO_PROBATION.crn, DEFAULT_DETAIL.copy(ProbationStatus.CURRENT)),
            Arguments.of(
                PersonGenerator.CURRENTLY_MANAGED.crn,
                DEFAULT_DETAIL.copy(ProbationStatus.CURRENT, inBreach = true)
            ),
            Arguments.of(
                PersonGenerator.PREVIOUSLY_MANAGED.crn,
                DEFAULT_DETAIL.copy(ProbationStatus.PREVIOUSLY_KNOWN, LocalDate.now().minusDays(7))
            ),
            Arguments.of(
                PersonGenerator.NO_SENTENCE.crn,
                DEFAULT_DETAIL.copy(ProbationStatus.NOT_SENTENCED, preSentenceActivity = true, awaitingPsr = true)
            )
        )
    }
}

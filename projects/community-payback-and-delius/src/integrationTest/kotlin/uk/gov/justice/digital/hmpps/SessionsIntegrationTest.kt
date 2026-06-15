package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.jsonPath
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest
class SessionsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `can retrieve sessions for multiple teams`() {
        mockMvc
            .getSessionsForTeams(listOf("N01UPW", "N02UP2"))
            .andExpect {
                content {
                    // N01UPW has 2 sessions, N02UP2 has 2 sessions = 4 total
                    jsonPath("content.size()", 4)
                    jsonPath("page.totalElements", 4)
                }
            }
    }

    @Test
    fun `can retrieve sessions for a single team via multi-team endpoint`() {
        mockMvc
            .getSessionsForTeams(listOf("N01UPW"))
            .andExpect {
                content {
                    jsonPath("content.size()", 2)
                    jsonPath("page.totalElements", 2)
                }
            }
    }

    @Test
    fun `404 returned when one of the team codes does not exist`() {
        mockMvc
            .get(
                "/sessions?teamCodes=N01UPW&teamCodes=DOESNOTEXIST&startDate=${
                    LocalDate.now().minusDays(3)
                }&endDate=${LocalDate.now().plusDays(3)}"
            ) {
                withToken()
            }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `can filter multi-team sessions by project type code`() {
        mockMvc
            .getSessionsForTeams(listOf("N01UPW", "N02UP2"), mapOf("typeCode" to "I"))
            .andExpect {
                content {
                    // Only UPW_PROJECT_1 (type I) belongs to N01UPW; N02UP2's project is type E-learning
                    jsonPath("content.size()", 2)
                }
            }
    }

    @Test
    fun `invalid sort property returns bad request on multi-team sessions endpoint`() {
        mockMvc
            .get(
                "/sessions?teamCodes=N01UPW&startDate=${LocalDate.now().minusDays(3)}&endDate=${
                    LocalDate.now().plusDays(3)
                }&sort=INVALID"
            ) {
                withToken()
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().run {
                assertThat(message).isEqualTo("Unsupported sort: INVALID")
            }
    }

    @Test
    fun `date range greater than 7 days returns bad request on multi-team sessions endpoint`() {
        mockMvc
            .get("/sessions?teamCodes=N01UPW&startDate=${LocalDate.now().minusDays(10)}&endDate=${LocalDate.now()}") {
                withToken()
            }
            .andExpect { status { isBadRequest() } }
    }

    private fun MockMvc.getSessionsForTeams(
        teamCodes: List<String>,
        queryParameters: Map<String, Any> = emptyMap(),
        startDate: LocalDate = LocalDate.now().minusDays(3),
        endDate: LocalDate = LocalDate.now().plusDays(3),
    ) = get("/sessions") {
        teamCodes.forEach { queryParam("teamCodes", it) }
        queryParam("startDate", startDate.toString())
        queryParam("endDate", endDate.toString())
        queryParameters.entries.forEach { queryParam(it.key, it.value.toString()) }
        withToken()
    }.andExpect { status { isOk() } }
}
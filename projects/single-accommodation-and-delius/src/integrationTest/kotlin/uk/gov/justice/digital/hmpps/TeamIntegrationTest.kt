package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class TeamIntegrationTest @Autowired constructor(private val mockMvc: MockMvc) {

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `returns case identifiers for cases assigned to a team`() {
        val team = TeamGenerator.DEFAULT
        mockMvc.get("/team/${team.code}/case-list") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "content": [
                            {
                              "crn": "A000001",
                              "prisonerNumber": "A0001AA"
                            },
                            {
                              "crn": "A000002",
                              "prisonerNumber": "A0002AA"
                            },
                            {
                              "crn": "A000004",
                              "prisonerNumber": "A0004AA"
                            },
                            {
                              "crn": "E123456",
                              "prisonerNumber": "E0001AA"
                            },
                            {
                              "crn": "R123456",
                              "prisonerNumber": "R0001AA"
                            }
                          ],
                          "page": {
                            "size": 50,
                            "number": 0,
                            "totalElements": 5,
                            "totalPages": 1
                          }
                        }
                    """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `returns 404 when team not found`() {
        val team = TeamGenerator.DEFAULT
        mockMvc.get("/team/NOTFOUND/case-list") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Team with code of NOTFOUND not found") }
            }
    }
}

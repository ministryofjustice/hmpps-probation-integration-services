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
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
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
        mockMvc.get("/team/NOTFOUND/case-list") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Team with code of NOTFOUND not found") }
            }
    }

    @Test
    fun `returns distinct staff across one or more teams`() {
        val defaultTeam = TeamGenerator.DEFAULT
        val otherTeam = TeamGenerator.OTHER_TEAM

        mockMvc.get("/team/staff?teamCodes=${defaultTeam.code}&teamCodes=${otherTeam.code}") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.staff.length()") { value(4) }
                jsonPath("$.staff[0].code") { value(StaffGenerator.TEAM_STAFF.code) }
                jsonPath("$.staff[0].username") { doesNotExist() }
                jsonPath("$.staff[1].code") { value(StaffGenerator.OTHER_TEAM_STAFF.code) }
                jsonPath("$.staff[1].username") { doesNotExist() }
                jsonPath("$.staff[2].code") { value(StaffGenerator.DEFAULT.code) }
                jsonPath("$.staff[2].username") { value("officer") }
                jsonPath("$.staff[3].code") { value(StaffGenerator.BOTH_TEAMS_STAFF.code) }
                jsonPath("$.staff[3].username") { value("bothteamsofficer") }
                jsonPath("$.page.size") { value(50) }
                jsonPath("$.page.number") { value(0) }
                jsonPath("$.page.totalElements") { value(4) }
                jsonPath("$.page.totalPages") { value(1) }
            }
    }
}

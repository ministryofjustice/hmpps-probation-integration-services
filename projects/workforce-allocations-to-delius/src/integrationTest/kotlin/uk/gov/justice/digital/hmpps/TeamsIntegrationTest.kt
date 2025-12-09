package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TeamsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `successful response`() {
        val team1 = TeamGenerator.DEFAULT.code
        val team2 = TeamGenerator.ALLOCATION_TEAM.code
        mockMvc.get("/teams") {
            withToken()
            param("teamCode", team1)
            param("teamCode", team2)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.teams.keys()") { value(setOf(team1, team2)) }
                jsonPath("$.teams.$team1.size()") { value(1) }
                jsonPath("$.teams.$team2.size()") { value(2) }
                jsonPath("$.teams.$team2[0].name.forename") { value("Joe") }
                jsonPath("$.teams.$team2[0].name.surname") { value("Bloggs") }
                jsonPath("$.teams.$team2[0].email") { value("example@example.com") }
            }
    }

    @Test
    fun `team codes can be empty`() {
        mockMvc.get("/teams") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.teams.keys()") { isEmpty() }
            }
    }

    @Test
    fun `get teams for staff`() {
        mockMvc.get("/staff/${StaffGenerator.STAFF_WITH_TEAM.code}/teams") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    json(
                        """
                        {
                          "datasets":[{"code":"N02","description":"NPS North East"}],
                          "teams": [
                            {
                              "code": "N03AAA",
                              "description": "Description for N03AAA",
                              "localAdminUnit": {
                                "code": "LAU1",
                                "description": "Some LAU",
                                "probationDeliveryUnit": {
                                  "code": "PDU1",
                                  "description": "Some PDU",
                                  "provider": {
                                    "code": "N02",
                                    "description": "NPS North East"
                                  }
                                }
                              }
                            }
                          ]
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `403 forbidden - staff end dated`() {
        mockMvc.get("/staff/${StaffGenerator.END_DATED_STAFF_WITH_TEAM.code}/teams") { withToken() }
            .andExpect { status { isForbidden() } }
    }
}

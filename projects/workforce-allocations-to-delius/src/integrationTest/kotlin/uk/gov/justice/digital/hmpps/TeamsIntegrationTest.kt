package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TeamsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `successful response`() {
        val team1 = TeamGenerator.DEFAULT.code
        val team2 = TeamGenerator.ALLOCATION_TEAM.code
        mockMvc.perform(get("/teams").withToken().param("teamCode", team1).param("teamCode", team2))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.teams.keys()").value(setOf(team1, team2)))
            .andExpect(jsonPath("$.teams.$team1.size()").value(1))
            .andExpect(jsonPath("$.teams.$team2.size()").value(2))
            .andExpect(jsonPath("$.teams.$team2[0].name.forename").value("Joe"))
            .andExpect(jsonPath("$.teams.$team2[0].name.surname").value("Bloggs"))
            .andExpect(jsonPath("$.teams.$team2[0].email").value("example@example.com"))
    }

    @Test
    fun `team codes can be empty`() {
        mockMvc.perform(get("/teams").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.teams.keys()").isEmpty)
    }

    @Test
    fun `get teams for staff`() {
        mockMvc.perform(get("/staff/${StaffGenerator.STAFF_WITH_TEAM.code}/teams").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(
                content().json(
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
            )
    }

    @Test
    fun `403 forbidden - staff end dated`() {
        mockMvc.perform(get("/staff/${StaffGenerator.END_DATED_STAFF_WITH_TEAM.code}/teams").withToken())
            .andExpect(status().isForbidden)
    }
}

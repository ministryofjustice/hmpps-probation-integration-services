package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ChoosePractitionerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `successful response`() {
        val person = PersonGenerator.DEFAULT
        val team1 = TeamGenerator.DEFAULT.code
        val team2 = TeamGenerator.ALLOCATION_TEAM.code
        mockMvc.get("/allocation-demand/choose-practitioner") {
            withToken()
            param("crn", person.crn)
            param("teamCode", team1)
            param("teamCode", team2)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.crn") { value(person.crn) }
                jsonPath("$.name.forename") { value(person.forename) }
                jsonPath("$.name.middleName") { value(person.secondName) }
                jsonPath("$.name.surname") { value(person.surname) }
                jsonPath("$.probationStatus.status") { value("PREVIOUSLY_MANAGED") }
                jsonPath("$.communityPersonManager.code") { value("N02UATU") }
                jsonPath("$.communityPersonManager.teamCode") { value("N02UAT") }
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
        val person = PersonGenerator.DEFAULT
        mockMvc.get("/allocation-demand/choose-practitioner") {
            withToken()
            param("crn", person.crn)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.teams.keys()") { isEmpty() }
            }
    }

    @Test
    fun `crn is required`() {
        mockMvc.get("/allocation-demand/choose-practitioner") {
            withToken()
        }
            .andExpect {
                status { is4xxClientError() }
            }
    }
}

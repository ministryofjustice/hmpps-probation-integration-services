package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ChoosePractitionerIntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var wireMockserver: WireMockServer

    @Test
    fun `successful response`() {
        val person = PersonGenerator.DEFAULT
        val team1 = TeamGenerator.DEFAULT.code
        val team2 = TeamGenerator.ALLOCATION_TEAM.code
        mockMvc.perform(
            get("/allocation-demand/choose-practitioner").withOAuth2Token(wireMockserver)
                .param("crn", person.crn)
                .param("teamCode", team1)
                .param("teamCode", team2)
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.crn").value(person.crn))
            .andExpect(jsonPath("$.name.forename").value(person.forename))
            .andExpect(jsonPath("$.name.middleName").value(person.secondName))
            .andExpect(jsonPath("$.name.surname").value(person.surname))
            .andExpect(jsonPath("$.probationStatus.status").value("UNKNOWN"))
            .andExpect(jsonPath("$.communityPersonManager.code").value("N02UATU"))
            .andExpect(jsonPath("$.communityPersonManager.teamCode").value("N02UAT"))
            .andExpect(jsonPath("$.teams.keys()").value(setOf(team1, team2, "all")))
            .andExpect(jsonPath("$.teams.all.size()").value(2))
            .andExpect(jsonPath("$.teams.$team1.size()").value(1))
            .andExpect(jsonPath("$.teams.$team2.size()").value(1))
            .andExpect(jsonPath("$.teams.$team2[0].name.forename").value("Brian"))
            .andExpect(jsonPath("$.teams.$team2[0].name.surname").value("Jones"))
    }

    @Test
    fun `team codes can be empty`() {
        val person = PersonGenerator.DEFAULT
        mockMvc.perform(
            get("/allocation-demand/choose-practitioner").withOAuth2Token(wireMockserver)
                .param("crn", person.crn)
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.teams.keys()").value(setOf("all")))
            .andExpect(jsonPath("$.teams.all.size()").value(0))
    }

    @Test
    fun `crn is required`() {
        mockMvc.perform(get("/allocation-demand/choose-practitioner").withOAuth2Token(wireMockserver))
            .andExpect(status().is4xxClientError)
    }
}

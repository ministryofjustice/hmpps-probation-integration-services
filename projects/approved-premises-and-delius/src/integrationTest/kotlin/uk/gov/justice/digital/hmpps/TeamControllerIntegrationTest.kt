package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TeamControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `teams managing case are returned successfully`() {
        val person = PersonGenerator.DEFAULT
        val allTeams = listOf(
            TeamGenerator.APPROVED_PREMISES_TEAM.code,
            TeamGenerator.NON_APPROVED_PREMISES_TEAM.code,
            TeamGenerator.UNALLOCATED.code
        )
        mockMvc
            .perform(get("/teams/managingCase/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.teamCodes.size()", equalTo(3)))
            .andExpect(jsonPath("$.teamCodes", equalTo(allTeams)))
    }

    @Test
    fun `staff code can be used to filter the returned teams`() {
        val person = PersonGenerator.DEFAULT
        mockMvc
            .perform(get("/teams/managingCase/${person.crn}?staffCode=KEY0001").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.teamCodes.size()", equalTo(1)))
            .andExpect(jsonPath("$.teamCodes[0]", equalTo(TeamGenerator.APPROVED_PREMISES_TEAM.code)))
    }
}

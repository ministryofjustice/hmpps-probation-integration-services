package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.model.ProvidersResponse
import uk.gov.justice.digital.hmpps.model.SessionsResponse
import uk.gov.justice.digital.hmpps.model.SupervisorsResponse
import uk.gov.justice.digital.hmpps.model.TeamsResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest
class ProvidersIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var wireMockServer: WireMockServer

    @Test
    fun `can retrieve all unpaid work teams for given provider`() {
        val response = mockMvc
            .perform(get("/providers/N01/teams").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<TeamsResponse>()

        assertThat(response.teams.size).isEqualTo(2)
    }

    @Test
    fun `empty list returned when provider has no upw teams`() {
        val response = mockMvc
            .perform(get("/providers/N99/teams").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<TeamsResponse>()

        assertThat(response.teams).isEmpty()
    }

    @Test
    fun `can retrieve all active providers for a user`() {
        val response = mockMvc
            .perform(get("/providers?username=DefaultUser").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ProvidersResponse>()

        assertThat(response.providers.size).isEqualTo(2)
    }

    @Test
    fun `404 returned when trying to get providers but user doesn't exist`() {
        mockMvc
            .perform(get("/providers?username=NonExistentUser").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `can retrieve all upw supervisors for provider and team`() {
        val response = mockMvc
            .perform(get("/providers/N01/teams/N01UPW/supervisors").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<SupervisorsResponse>()

        assertThat(response.supervisors.size).isEqualTo(2)
    }

    @Test
    fun `can retrieve all upw sessions for provider and team`() {
        val response = mockMvc
            .perform(
                get(
                    "/providers/N01/teams/N01UPW/sessions?startDate=${
                        LocalDate.now().minusDays(3)
                    }&endDate=${LocalDate.now().plusDays(3)}"
                ).withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<SessionsResponse>()

        assertThat(response.sessions.size).isEqualTo(2)
    }
}

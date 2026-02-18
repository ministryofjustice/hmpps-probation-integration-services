package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.model.ProvidersResponse
import uk.gov.justice.digital.hmpps.model.SessionsResponse
import uk.gov.justice.digital.hmpps.model.SupervisorsResponse
import uk.gov.justice.digital.hmpps.model.TeamsResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest
class ProvidersIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `can retrieve all unpaid work teams for given provider`() {
        val response = mockMvc
            .get("/providers/N01/teams") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<TeamsResponse>()

        assertThat(response.teams.size).isEqualTo(2)
    }

    @Test
    fun `empty list returned when provider has no upw teams`() {
        val response = mockMvc
            .get("/providers/N99/teams") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<TeamsResponse>()

        assertThat(response.teams).isEmpty()
    }

    @Test
    fun `can retrieve all active providers for a user`() {
        val response = mockMvc
            .get("/providers?username=DefaultUser") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<ProvidersResponse>()

        assertThat(response.providers.size).isEqualTo(2)
    }

    @Test
    fun `404 returned when trying to get providers but user doesn't exist`() {
        mockMvc
            .get("/providers?username=NonExistentUser") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `can retrieve all upw supervisors for provider and team`() {
        val response = mockMvc
            .get("/providers/N01/teams/N01UPW/supervisors") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<SupervisorsResponse>()

        assertThat(response.supervisors.map { listOf(it.code, it.unallocated) }).containsExactlyInAnyOrder(
            listOf(StaffGenerator.DEFAULT_STAFF.code, false),
            listOf(StaffGenerator.SECOND_STAFF.code, false),
            listOf(StaffGenerator.UNALLOCATED_STAFF.code, true),
        )
    }

    @Test
    fun `can retrieve all upw projects for provider and team`() {
        mockMvc
            .get("/providers/N02/teams/N02UP2/projects?typeCode=I&page=0&size=10") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.content.length()") { value(1) }
                jsonPath("$.content[0].project.code") { value(UPWGenerator.UPW_PROJECT_2.code) }
                jsonPath("$.content[0].project.type.code") { value("I") }
                jsonPath("$.content[0].project.team.code") { value("N02UP2") }
                jsonPath("$.content[0].overdueOutcomesCount") { value(11) }
                jsonPath("$.content[0].oldestOverdueInDays") { value(1) }
            }
    }

    @Test
    fun `can sort projects by name`() {
        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?typeCode=G&sort=name,asc") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.content[0].project.name") { value("Default UPW Project") }
                jsonPath("$.content[1].project.name") { value("Third UPW Project") }
            }
    }

    @Test
    fun `can sort projects by overdue stats`() {
        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?sort=overdueOutcomesCount,desc") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.content[0].overdueOutcomesCount") { value(1) }
                jsonPath("$.content[1].overdueOutcomesCount") { value(0) }
            }

        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?sort=oldestOverdueInDays") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.content[0].oldestOverdueInDays") { value(0) }
                jsonPath("$.content[1].oldestOverdueInDays") { value(7) }
            }
    }

    @Test
    fun `invalid sort property returns bad request`() {
        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?sort=INVALID") { withToken() }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().run {
                assertThat(message).isEqualTo("Unsupported sort: INVALID")
            }
    }

    @Test
    fun `can retrieve all upw sessions for provider and team`() {
        val response = mockMvc
            .get(
                "/providers/N01/teams/N01UPW/sessions" +
                    "?startDate=${LocalDate.now().minusDays(3)}&endDate=${LocalDate.now().plusDays(3)}"
            ) { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<SessionsResponse>()

        assertThat(response.sessions.size).isEqualTo(2)
        assertThat(response.sessions.map { it.date }).isEqualTo(listOf(LocalDate.now(), LocalDate.now().plusDays(1)))
    }
}

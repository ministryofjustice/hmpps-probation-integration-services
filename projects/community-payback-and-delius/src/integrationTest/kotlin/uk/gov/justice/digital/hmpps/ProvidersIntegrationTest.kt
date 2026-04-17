package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.model.PickUpLocationsResponse
import uk.gov.justice.digital.hmpps.model.ProvidersResponse
import uk.gov.justice.digital.hmpps.model.SupervisorsResponse
import uk.gov.justice.digital.hmpps.model.TeamsResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.jsonPath
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
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<TeamsResponse>()

        assertThat(response.teams.size).isEqualTo(2)
    }

    @Test
    fun `empty list returned when provider has no upw teams`() {
        val response = mockMvc
            .get("/providers/N99/teams") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<TeamsResponse>()

        assertThat(response.teams).isEmpty()
    }

    @Test
    fun `can retrieve all active providers for a user`() {
        val response = mockMvc
            .get("/providers?username=DefaultUser") { withToken() }
            .andExpect { status { isOk() } }
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
            .get("/providers/N01/teams/N01UPW/projects?sort=name,asc") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.content[0].project.code") { value(UPWGenerator.UPW_PROJECT_1.code) }
                jsonPath("$.content[0].project.type.code") { value("I") }
                jsonPath("$.content[0].project.team.code") { value("N01UPW") }
                jsonPath("$.content[0].overdueOutcomesCount") { value(1) }
                jsonPath("$.content[0].oldestOverdueInDays") { value(7) }
            }
    }

    @Test
    fun `can set maximum for overdueDays`() {
        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?overdueDays=8") { withToken() }
            .andExpect {
                content {
                    jsonPath("content.size()", 2)
                    jsonPath("content[*].oldestOverdueInDays", hasItem(7))
                }
            }
        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?overdueDays=6") { withToken() }
            .andExpect {
                content {
                    jsonPath("content.size()", 2)
                    jsonPath("content[*].oldestOverdueInDays", not(hasItem(7)))
                }
            }
    }

    @Test
    fun `can sort projects by name`() {
        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?typeCode=I&sort=name,asc") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(1) }
                jsonPath("$.content[0].project.name") { value("Default UPW Project") }
                jsonPath("$.content[0].project.type.name") { value("Individual Placement") }
            }
    }

    @Test
    fun `can sort projects by overdue stats`() {
        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?sort=overdueOutcomesCount,desc") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.content[0].overdueOutcomesCount") { value(1) }
                jsonPath("$.content[1].overdueOutcomesCount") { value(0) }
            }

        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?sort=oldestOverdueInDays") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.content[0].oldestOverdueInDays") { value(0) }
                jsonPath("$.content[1].oldestOverdueInDays") { value(7) }
            }
    }

    @Test
    fun `invalid projects sort property returns bad request`() {
        mockMvc
            .get("/providers/N01/teams/N01UPW/projects?sort=INVALID") { withToken() }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().run {
                assertThat(message).isEqualTo("Unsupported sort: INVALID")
            }
    }

    @Test
    fun `can paginate upw sessions`() {
        mockMvc
            .getSessions("N01", "N01UPW")
            .andExpect {
                content {
                    jsonPath("content.size()", 2)
                    jsonPath("sessions.size()", 2)
                    jsonPath("page.number", 0)
                    jsonPath("page.size", 10)
                    jsonPath("page.totalElements", 2)
                    jsonPath("page.totalPages", 1)
                }
            }
        mockMvc
            .getSessions("N01", "N01UPW", mapOf("size" to 1))
            .andExpect {
                content {
                    jsonPath("content.size()", 1)
                    jsonPath("sessions.size()", 2)
                    jsonPath("page.number", 0)
                    jsonPath("page.size", 1)
                    jsonPath("page.totalElements", 2)
                    jsonPath("page.totalPages", 2)
                }
            }
    }

    @Test
    fun `can sort sessions by date`() {
        val expected = listOf(LocalDate.now().plusDays(1).toString(), LocalDate.now().toString())
        mockMvc
            .getSessions("N01", "N01UPW", mapOf("sort" to "date,desc"))
            .andExpect { content { jsonPath("content[*].date", expected) } }
        mockMvc
            .getSessions("N01", "N01UPW", mapOf("sort" to "date,asc"))
            .andExpect { content { jsonPath("content[*].date", expected.reversed()) } }
    }

    @Test
    fun `can sort sessions by allocated count`() {
        mockMvc
            .getSessions("N02", "N02UP2", mapOf("sort" to "allocatedCount,desc"))
            .andExpect { content { jsonPath("content[*].allocatedCount", listOf(2, 1)) } }
        mockMvc
            .getSessions("N02", "N02UP2", mapOf("sort" to "allocatedCount,asc"))
            .andExpect { content { jsonPath("content[*].allocatedCount", listOf(1, 2)) } }
    }

    @Test
    fun `can sort sessions by outcome count`() {
        mockMvc
            .getSessions("N01", "N01UPW", mapOf("sort" to "outcomeCount,desc"))
            .andExpect { content { jsonPath("content[*].outcomeCount", listOf(2, 1)) } }
        mockMvc
            .getSessions("N01", "N01UPW", mapOf("sort" to "outcomeCount,asc"))
            .andExpect { content { jsonPath("content[*].outcomeCount", listOf(1, 2)) } }
    }

    @Test
    fun `invalid sessions sort property returns bad request`() {
        mockMvc
            .get("/providers/N01/teams/N01UPW/sessions?startDate=2000-01-01&endDate=2000-01-02&sort=INVALID") { withToken() }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().run {
                assertThat(message).isEqualTo("Unsupported sort: INVALID")
            }
    }

    @Test
    fun `can filter sessions by project type codes`() {
        mockMvc
            .getSessions("N01", "N01UPW", mapOf("typeCode" to "I"))
            .andExpect { content { jsonPath("content.size()", 2) } }

        mockMvc
            .getSessions("N01", "N01UPW", mapOf("typeCode" to "G"))
            .andExpect { content { jsonPath("content.size()", 0) } }
    }

    @Test
    fun `can get pickup locations by team`() {
        val actual = mockMvc
            .get("/providers/team/${TeamGenerator.OTHER_PROVIDER_TEAM.code}/locations") { withToken() }
            .andExpect {
                status { isOk() }
            }
            .andReturn().response.contentAsJson<PickUpLocationsResponse>()
        assertThat(actual.locations.size).isEqualTo(1)
        assertThat(actual.locations[0].description).isEqualTo("City Location")
        assertThat(actual.locations[0].code).isEqualTo("LOC0001")
        assertThat(actual.locations[0].postCode).isEqualTo("ZY98XW")
    }

    private fun MockMvc.getSessions(
        providerCode: String,
        teamCode: String,
        queryParameters: Map<String, Any> = emptyMap(),
        startDate: LocalDate = LocalDate.now().minusDays(3),
        endDate: LocalDate = LocalDate.now().plusDays(3),
    ) = get("/providers/$providerCode/teams/$teamCode/sessions?") {
        queryParam("startDate", startDate.toString())
        queryParam("endDate", endDate.toString())
        queryParameters.entries.forEach { queryParam(it.key, it.value.toString()) }
        withToken()
    }.andExpect { status { isOk() } }
}

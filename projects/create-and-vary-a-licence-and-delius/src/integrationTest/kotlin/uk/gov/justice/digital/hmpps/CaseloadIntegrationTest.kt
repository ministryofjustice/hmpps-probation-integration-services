package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.junit.jupiter.EnabledIf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.ManagedOffender
import uk.gov.justice.digital.hmpps.api.model.ManagedOffenderSummary
import uk.gov.justice.digital.hmpps.api.model.SearchRequest
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.CASELOAD_ROLE_OM_1
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.CASELOAD_ROLE_OM_2
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.CASELOAD_ROLE_OM_3
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.CASELOAD_ROLE_OM_4
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.STAFF1
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.STAFF2
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.TEAM1
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.generateManagedOffender
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.generateManagedOffenderSummary
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CaseloadIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {

    @ParameterizedTest
    @MethodSource("trimmedCaseloadArgs")
    fun getTrimmedManagedOffenders(url: String, expected: List<ManagedOffenderSummary>?) {
        val res = mockMvc.get(url) { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<ManagedOffenderSummary>>()
        assertThat(res, equalTo(expected))
    }

    @ParameterizedTest
    @MethodSource("fullCaseloadArgs")
    fun getManagedOffenders(url: String, expected: List<ManagedOffender>?) {
        val res = mockMvc.get(url) { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<ManagedOffender>>()
        assertThat(res, equalTo(expected))
    }

    @Test
    fun `team managed offenders can be sorted`() {
        mockMvc.post("/staff/byid/${STAFF1.id}/caseload/team-managed-offenders?sort=surname,desc") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.page.size") { value(100) }
                jsonPath("$.page.number") { value(0) }
                jsonPath("$.page.totalElements") { value(4) }
                jsonPath("$.page.totalPages") { value(1) }
                jsonPath("$.content[*].crn") { value(equalTo(listOf("crn0077", "crn0022", "crn0078", "crn0001"))) }
                jsonPath("$.content[*].name.surname") { value(equalTo(listOf("mys", "Doe", "Doe", "Brown"))) }
                jsonPath("$.content[0].staff.code") { value("STCDE02") }
                jsonPath("$.content[0].staff.name.forename") { value("Joe") }
                jsonPath("$.content[0].staff.unallocated") { value(false) }
                jsonPath("$.content[0].team.code") { value("N01BDT") }
                jsonPath("$.content[0].team.description") { value("Description of N01BDT") }
                jsonPath("$.content[0].staff.teams") { doesNotExist() }
                jsonPath("$.content[0].team.district") { doesNotExist() }
            }
    }

    @Test
    @EnabledIf("#{environment.acceptsProfiles('oracle')}", loadContext = true)
    fun `team managed offenders can be filtered by substring`() {
        mockMvc.post("/staff/byid/${STAFF1.id}/caseload/team-managed-offenders") {
            withToken()
            json = SearchRequest("john b")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.page.size") { value(100) }
                jsonPath("$.page.number") { value(0) }
                jsonPath("$.page.totalElements") { value(1) }
                jsonPath("$.page.totalPages") { value(1) }
                jsonPath("$.content[0].name.forename") { value("John") }
                jsonPath("$.content[0].name.surname") { value("Brown") }
            }
    }

    companion object {
        @JvmStatic
        fun trimmedCaseloadArgs(): List<Arguments> = listOf(
            Arguments.of(
                "/staff/byid/${STAFF2.id}/caseload/managed-offenders", listOf(
                    generateManagedOffenderSummary(CASELOAD_ROLE_OM_3),
                    generateManagedOffenderSummary(CASELOAD_ROLE_OM_4)
                )
            ), Arguments.of(
                "/team/N01BDT/caseload/managed-offenders", listOf(
                    generateManagedOffenderSummary(CASELOAD_ROLE_OM_3),
                    generateManagedOffenderSummary(CASELOAD_ROLE_OM_2),
                    generateManagedOffenderSummary(CASELOAD_ROLE_OM_1)
                )
            ), Arguments.of(
                "/team/N02BDT/caseload/managed-offenders", listOf(
                    generateManagedOffenderSummary(CASELOAD_ROLE_OM_4)
                )
            ), Arguments.of(
                "/team/N03BDT/caseload/managed-offenders", listOf<ManagedOffenderSummary>()
            )
        )

        @JvmStatic
        fun fullCaseloadArgs(): List<Arguments> = listOf(
            Arguments.of("/staff/STCDEXX/caseload/managed-offenders", listOf<ManagedOffender>()), Arguments.of(
                "/staff/STCDE01/caseload/managed-offenders", listOf(
                    generateManagedOffender(CASELOAD_ROLE_OM_1, STAFF1, DEFAULT_TEAM),
                    generateManagedOffender(CASELOAD_ROLE_OM_2, STAFF1, DEFAULT_TEAM)
                )
            ), Arguments.of(
                "/staff/STCDE02/caseload/managed-offenders", listOf(
                    generateManagedOffender(CASELOAD_ROLE_OM_3, STAFF2, DEFAULT_TEAM),
                    generateManagedOffender(CASELOAD_ROLE_OM_4, STAFF2, TEAM1)
                )
            )
        )
    }
}

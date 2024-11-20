package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.ManagedOffender
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.CASELOAD_ROLE_OM_1
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.CASELOAD_ROLE_OM_2
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.CASELOAD_ROLE_OM_3
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.CASELOAD_ROLE_OM_4
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.STAFF1
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.STAFF2
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.TEAM1
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.generateManagedOffender
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CaseloadIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @ParameterizedTest
    @MethodSource("caseloadArgs")
    fun getManagedOffenders(url: String, expected: List<ManagedOffender>?) {
        val res = mockMvc.perform(get(url).withToken()).andExpect(status().isOk)
            .andReturn().response.contentAsJson<List<ManagedOffender>>()
        assertThat(res, equalTo(expected))
    }

    companion object {
        @JvmStatic
        fun caseloadArgs(): List<Arguments> = listOf(
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
            ), Arguments.of(
                "/team/N01BDT/caseload/managed-offenders", listOf(
                    generateManagedOffender(CASELOAD_ROLE_OM_3, STAFF2, DEFAULT_TEAM),
                    generateManagedOffender(CASELOAD_ROLE_OM_2, STAFF1, DEFAULT_TEAM),
                    generateManagedOffender(CASELOAD_ROLE_OM_1, STAFF1, DEFAULT_TEAM)
                )
            ), Arguments.of(
                "/team/N02BDT/caseload/managed-offenders", listOf(
                    generateManagedOffender(CASELOAD_ROLE_OM_4, STAFF2, TEAM1)
                )
            ), Arguments.of(
                "/team/N03BDT/caseload/managed-offenders", listOf<ManagedOffender>()
            )
        )
    }
}

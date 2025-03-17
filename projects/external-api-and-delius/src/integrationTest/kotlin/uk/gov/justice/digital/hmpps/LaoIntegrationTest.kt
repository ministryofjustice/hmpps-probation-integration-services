package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.EXCLUSION
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.RESTRICTION
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.RESTRICTION_EXCLUSION
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.model.CrnRequest
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import uk.gov.justice.digital.hmpps.user.AuditUser

internal class LaoIntegrationTest : BaseIntegrationTest() {
    @ParameterizedTest
    @MethodSource("laoUserCases")
    fun `LAO results are appropriately returned`(user: AuditUser?, person: Person, access: CaseAccess) {
        val queryParam = user?.let { "?username=${user.username}" } ?: ""
        val response = mockMvc
            .perform(
                post("/probation-cases/access$queryParam")
                    .withJson(CrnRequest(listOf(person.crn)))
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<UserAccess>()

        Assertions.assertThat(response).isEqualTo(UserAccess(listOf(access)))
    }

    companion object {
        @JvmStatic
        fun laoUserCases() = listOf(
            Arguments.of(UserGenerator.LIMITED_ACCESS_USER, EXCLUSION, EXCLUSION.lao()),
            Arguments.of(UserGenerator.LIMITED_ACCESS_USER, RESTRICTION, RESTRICTION.lao()),
            Arguments.of(UserGenerator.LIMITED_ACCESS_USER, RESTRICTION_EXCLUSION, RESTRICTION_EXCLUSION.lao()),
            Arguments.of(UserGenerator.AUDIT_USER, EXCLUSION, EXCLUSION.noLao()),
            Arguments.of(UserGenerator.AUDIT_USER, RESTRICTION, RESTRICTION.noLao()),
            Arguments.of(UserGenerator.AUDIT_USER, RESTRICTION_EXCLUSION, RESTRICTION_EXCLUSION.noLao()),
            Arguments.of(null, EXCLUSION, EXCLUSION.lao()),
            Arguments.of(null, RESTRICTION, RESTRICTION.lao()),
            Arguments.of(null, RESTRICTION_EXCLUSION, RESTRICTION_EXCLUSION.lao()),
        )

        private fun Person.lao(): CaseAccess =
            CaseAccess(
                crn,
                exclusionMessage != null,
                restrictionMessage != null,
                exclusionMessage,
                restrictionMessage
            )

        private fun Person.noLao(): CaseAccess =
            CaseAccess(crn, false, false, null, null)
    }

    @Test
    fun `validates that between 1 and 500 crns are provided`() {
        mockMvc
            .perform(
                post("/probation-cases/access?username=${UserGenerator.AUDIT_USER.username.lowercase()}")
                    .withToken()
                    .withJson(CrnRequest(listOf()))
            ).andExpect { status().isBadRequest }
    }
}
package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.EXCLUSION
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.RESTRICTION
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.RESTRICTION_EXCLUSION
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.Person
import uk.gov.justice.digital.hmpps.model.CaseAccess
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import uk.gov.justice.digital.hmpps.user.AuditUser

internal class LaoIntegrationTest : BaseIntegrationTest() {
    @ParameterizedTest
    @MethodSource("laoUserCases")
    fun `LAO results are appropriately returned`(user: AuditUser, person: Person, access: CaseAccess) {
        val response = mockMvc
            .perform(get("/users/${user.username}/access/${person.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<CaseAccess>()

        assertThat(response).isEqualTo(access)
    }

    companion object {
        @JvmStatic
        fun laoUserCases() = listOf(
            Arguments.of(UserGenerator.LIMITED_ACCESS_USER, EXCLUSION, EXCLUSION.lao()),
            Arguments.of(UserGenerator.LIMITED_ACCESS_USER, RESTRICTION, RESTRICTION.lao()),
            Arguments.of(UserGenerator.LIMITED_ACCESS_USER, RESTRICTION_EXCLUSION, RESTRICTION_EXCLUSION.lao()),
            Arguments.of(UserGenerator.NON_LAO_USER, EXCLUSION, EXCLUSION.noLao()),
            Arguments.of(UserGenerator.NON_LAO_USER, RESTRICTION, RESTRICTION.noLao()),
            Arguments.of(UserGenerator.NON_LAO_USER, RESTRICTION_EXCLUSION, RESTRICTION_EXCLUSION.noLao()),
        )

        private fun Person.lao(): CaseAccess =
            CaseAccess(crn, exclusionMessage != null, restrictionMessage != null, exclusionMessage, restrictionMessage)

        private fun Person.noLao(): CaseAccess =
            CaseAccess(crn, false, false, null, null)
    }
}
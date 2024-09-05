package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.CaseGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserIntergrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `limited access controls do not prevent legitimate access for exclusion`() {
        val caseAccess = mockMvc.perform(
            get("/users/${UserGenerator.AUDIT_USER.username.lowercase()}/access/${CaseGenerator.EXCLUSION.crn}")
                .withToken()
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()
        assertThat(
            caseAccess,
            equalTo(CaseAccess(CaseGenerator.EXCLUSION.crn, false, false))
        )
    }

    @Test
    fun `limited access controls do not prevent legitimate access for restriction`() {
        val caseAccess = mockMvc.perform(
            get("/users/${UserGenerator.AUDIT_USER.username.lowercase()}/access/${CaseGenerator.RESTRICTION.crn}")
                .withToken()
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()
        assertThat(
            caseAccess,
            equalTo(CaseAccess(CaseGenerator.RESTRICTION.crn, false, false))
        )
    }

    @Test
    fun `limited access controls are correctly returned for an exclusion`() {
        val caseAccess = mockMvc.perform(
            get("/users/${UserGenerator.LIMITED_ACCESS_USER.username.lowercase()}/access/${CaseGenerator.EXCLUSION.crn}")
                .withToken()
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()

        val excludedCrn = CaseGenerator.EXCLUSION.crn
        assertThat(
            caseAccess,
            equalTo(
                CaseAccess(
                    excludedCrn,
                    userExcluded = true,
                    userRestricted = false,
                    exclusionMessage = CaseGenerator.EXCLUSION.exclusionMessage
                )
            )
        )
    }

    @Test
    fun `limited access controls are correctly returned for a restiction`() {
        val caseAccess = mockMvc.perform(
            get("/users/${UserGenerator.LIMITED_ACCESS_USER.username.lowercase()}/access/${CaseGenerator.RESTRICTION.crn}")
                .withToken()
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()

        val restrictedCrn = CaseGenerator.RESTRICTION.crn
        assertThat(
            caseAccess,
            equalTo(
                CaseAccess(
                    restrictedCrn,
                    userExcluded = false,
                    userRestricted = true,
                    restrictionMessage = CaseGenerator.RESTRICTION.restrictionMessage
                )
            )
        )
    }

    @Test
    fun `limited access controls are correctly returned for no limit crn`() {
        val caseAccess = mockMvc.perform(
            get("/users/${UserGenerator.LIMITED_ACCESS_USER.username.lowercase()}/access/${CaseGenerator.DEFAULT.crn}")
                .withToken()
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()

        val noLimitCrn = CaseGenerator.DEFAULT.crn
        assertThat(
            caseAccess,
            equalTo(
                CaseAccess(
                    noLimitCrn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
    }

    @Test
    fun `limited access controls are correctly returned for both exclusions and restrictions by id`() {
        val caseAccess = mockMvc.perform(
            get("/users/${UserGenerator.LIMITED_ACCESS_USER.id}/access/${CaseGenerator.RESTRICTION_EXCLUSION.crn}")
                .withToken()
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()

        val bothCrn = CaseGenerator.RESTRICTION_EXCLUSION.crn
        assertThat(
            caseAccess,
            equalTo(
                CaseAccess(
                    bothCrn,
                    userExcluded = true,
                    userRestricted = true,
                    exclusionMessage = CaseGenerator.RESTRICTION_EXCLUSION.exclusionMessage,
                    restrictionMessage = CaseGenerator.RESTRICTION_EXCLUSION.restrictionMessage
                )
            )
        )
    }
}

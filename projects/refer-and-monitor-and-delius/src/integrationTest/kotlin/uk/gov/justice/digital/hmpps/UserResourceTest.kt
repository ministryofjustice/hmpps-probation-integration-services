package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.api.model.ManagedCases
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.UserDetail
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserResourceTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `correctly returns cases managed by a given user`() {
        val managedCases = mockMvc.get("/users/john-smith/managed-cases") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<ManagedCases>()

        assertThat(managedCases.managedCases.size, equalTo(2))
        assertThat(managedCases.managedCases, hasItem(CaseIdentifier(PersonGenerator.COMMUNITY_RESPONSIBLE.crn)))
        assertThat(managedCases.managedCases, hasItem(CaseIdentifier(PersonGenerator.COMMUNITY_NOT_RESPONSIBLE.crn)))
    }

    @Test
    fun `limited access controls are correctly returned`() {
        val userAccess = mockMvc.post("/users/${UserGenerator.LIMITED_ACCESS_USER.username.lowercase()}/access") {
            withToken()
            json = listOf(
                PersonGenerator.EXCLUSION.crn,
                PersonGenerator.RESTRICTION.crn,
                PersonGenerator.DEFAULT.crn,
                PersonGenerator.RESTRICTION_EXCLUSION.crn
            )
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserAccess>()

        val result: Map<String, CaseAccess> = userAccess.access.associateBy { it.crn }

        val excludedCrn = PersonGenerator.EXCLUSION.crn
        assertThat(
            result[excludedCrn],
            equalTo(
                CaseAccess(
                    excludedCrn,
                    userExcluded = true,
                    userRestricted = false,
                    exclusionMessage = PersonGenerator.EXCLUSION.exclusionMessage
                )
            )
        )

        val restrictedCrn = PersonGenerator.RESTRICTION.crn
        assertThat(
            result[restrictedCrn],
            equalTo(
                CaseAccess(
                    restrictedCrn,
                    userExcluded = false,
                    userRestricted = true,
                    restrictionMessage = PersonGenerator.RESTRICTION.restrictionMessage
                )
            )
        )

        val noLimitCrn = PersonGenerator.DEFAULT.crn
        assertThat(
            result[noLimitCrn],
            equalTo(
                CaseAccess(
                    noLimitCrn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )

        val bothCrn = PersonGenerator.RESTRICTION_EXCLUSION.crn
        assertThat(
            result[bothCrn],
            equalTo(
                CaseAccess(
                    bothCrn,
                    userExcluded = true,
                    userRestricted = true,
                    exclusionMessage = PersonGenerator.RESTRICTION_EXCLUSION.exclusionMessage,
                    restrictionMessage = PersonGenerator.RESTRICTION_EXCLUSION.restrictionMessage
                )
            )
        )
    }

    @Test
    fun `limited access controls do not prevent legitimate access`() {
        val userAccess = mockMvc.post("/users/${UserGenerator.AUDIT_USER.username.lowercase()}/access") {
            withToken()
            json = listOf(
                PersonGenerator.EXCLUSION.crn,
                PersonGenerator.RESTRICTION.crn,
                PersonGenerator.DEFAULT.crn
            )
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserAccess>()

        val result: Map<String, CaseAccess> = userAccess.access.associateBy { it.crn }

        val exclusionCrn = PersonGenerator.EXCLUSION.crn
        assertThat(
            result[exclusionCrn],
            equalTo(CaseAccess(exclusionCrn, false, false))
        )
        val restrictionCrn = PersonGenerator.RESTRICTION.crn
        assertThat(
            result[restrictionCrn],
            equalTo(CaseAccess(restrictionCrn, false, false))
        )
        val default = PersonGenerator.DEFAULT.crn
        assertThat(
            result[default],
            equalTo(CaseAccess(default, false, false))
        )
    }

    @Test
    fun `validates that between 1 and 500 crns are provided`() {
        mockMvc.post("/users/${UserGenerator.AUDIT_USER.username.lowercase()}/access") {
            withToken()
            json = listOf<String>()
        }
            .andExpect {
                status { isBadRequest() }
                content {
                    json(
                        """
                        {"status":400,"message":"userAccessCheck.crns: Please provide between 1 and 500 crns"}
                        """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `user details not found returns 404`() {
        mockMvc.get("/users/non-existent-user/details") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `user details not found returns 404 from id`() {
        mockMvc.get("/users/829185656291/details") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `user details are correctly returned`() {
        mockMvc.get("/users/john-smith/details") { withToken() }
            .andExpectJson(UserDetail("john-smith", Name("John", "Smith"), "john.smith@moj.gov.uk"))
    }

    @Test
    fun `user details are correctly returned from id`() {
        mockMvc.get("/users/${ProviderGenerator.JOHN_SMITH_USER.id}/details") { withToken() }
            .andExpectJson(UserDetail("john-smith", Name("John", "Smith"), "john.smith@moj.gov.uk"))
    }
}

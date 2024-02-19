package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class LimitedAccessTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `limited access controls are correctly returned with username`() {
        val res = mockMvc.perform(
            post("/users/access?username=${LimitedAccessGenerator.LIMITED_ACCESS_USER.username}")
                .withToken()
                .withJson(
                    listOf(
                        LimitedAccessGenerator.EXCLUDED_CASE.crn,
                        LimitedAccessGenerator.RESTRICTED_CASE.crn,
                        LimitedAccessGenerator.UNLIMITED_ACCESS.crn
                    )
                )
        ).andReturn().response.contentAsJson<UserAccess>()

        validateResults(res)
    }

    @Test
    fun `limited access controls are correctly returned without username`() {
        val res = mockMvc.perform(
            post("/users/access")
                .withToken()
                .withJson(
                    listOf(
                        LimitedAccessGenerator.EXCLUDED_CASE.crn,
                        LimitedAccessGenerator.RESTRICTED_CASE.crn,
                        LimitedAccessGenerator.UNLIMITED_ACCESS.crn
                    )
                )
        ).andReturn().response.contentAsJson<UserAccess>()

        validateResults(res)
    }

    private fun validateResults(result: UserAccess) {
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.EXCLUDED_CASE.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.EXCLUDED_CASE.crn,
                    userExcluded = true,
                    userRestricted = false,
                    exclusionMessage = LimitedAccessGenerator.EXCLUDED_CASE.exclusionMessage
                )
            )
        )
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.RESTRICTED_CASE.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.RESTRICTED_CASE.crn,
                    userExcluded = false,
                    userRestricted = true,
                    restrictionMessage = LimitedAccessGenerator.RESTRICTED_CASE.restrictionMessage
                )
            )
        )
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.UNLIMITED_ACCESS.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.UNLIMITED_ACCESS.crn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
    }

    @Test
    fun `limited access controls are correctly returned with full access`() {
        val result = mockMvc.perform(
            post("/users/access?username=${LimitedAccessGenerator.FULL_ACCESS_USER.username}")
                .withToken()
                .withJson(
                    listOf(
                        LimitedAccessGenerator.EXCLUDED_CASE.crn,
                        LimitedAccessGenerator.RESTRICTED_CASE.crn,
                        LimitedAccessGenerator.UNLIMITED_ACCESS.crn
                    )
                )
        ).andReturn().response.contentAsJson<UserAccess>()

        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.EXCLUDED_CASE.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.EXCLUDED_CASE.crn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.RESTRICTED_CASE.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.RESTRICTED_CASE.crn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.UNLIMITED_ACCESS.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.UNLIMITED_ACCESS.crn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
    }
}

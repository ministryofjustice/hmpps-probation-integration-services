package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.api.model.CaseAccessList
import uk.gov.justice.digital.hmpps.api.model.User
import uk.gov.justice.digital.hmpps.api.model.UserAccess
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get all users`() {
        mockMvc.perform(get("/users").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$[0].username", equalToIgnoringCase("JoeBloggs")))
    }

    @Test
    fun `limited access controls are correctly returned`() {
        val result = mockMvc.perform(
            post("/users/limited-access?username=${LimitedAccessGenerator.EXCLUSION.user.username}")
                .withToken()
                .withJson(
                    listOf(
                        PersonGenerator.EXCLUSION.crn,
                        PersonGenerator.RESTRICTION.crn,
                        PersonGenerator.DEFAULT.crn,
                        PersonGenerator.RESTRICTION_EXCLUSION.crn
                    )
                )
        ).andReturn().response.contentAsJson<UserAccess>()

        assertThat(
            result.access.first { it.crn == PersonGenerator.EXCLUSION.crn },
            equalTo(
                CaseAccess(
                    PersonGenerator.EXCLUSION.crn,
                    userExcluded = true,
                    userRestricted = false,
                    exclusionMessage = PersonGenerator.EXCLUSION.exclusionMessage
                )
            )
        )
        assertThat(
            result.access.first { it.crn == PersonGenerator.RESTRICTION.crn },
            equalTo(
                CaseAccess(
                    PersonGenerator.RESTRICTION.crn,
                    userExcluded = false,
                    userRestricted = true,
                    restrictionMessage = PersonGenerator.RESTRICTION.restrictionMessage
                )
            )
        )
        assertThat(
            result.access.first { it.crn == PersonGenerator.DEFAULT.crn },
            equalTo(
                CaseAccess(
                    PersonGenerator.DEFAULT.crn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
        assertThat(
            result.access.first { it.crn == PersonGenerator.RESTRICTION_EXCLUSION.crn },
            equalTo(
                CaseAccess(
                    PersonGenerator.RESTRICTION_EXCLUSION.crn,
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
        val result = mockMvc.perform(
            post("/users/limited-access?username=${LimitedAccessGenerator.RESTRICTION.user.username}")
                .withToken()
                .withJson(
                    listOf(
                        PersonGenerator.EXCLUSION.crn,
                        PersonGenerator.RESTRICTION.crn,
                        PersonGenerator.DEFAULT.crn
                    )
                )
        ).andReturn().response.contentAsJson<UserAccess>()

        assertThat(
            result.access.first { it.crn == PersonGenerator.EXCLUSION.crn },
            equalTo(CaseAccess(PersonGenerator.EXCLUSION.crn, userExcluded = false, userRestricted = false))
        )
        assertThat(
            result.access.first { it.crn == PersonGenerator.RESTRICTION.crn },
            equalTo(CaseAccess(PersonGenerator.RESTRICTION.crn, userExcluded = false, userRestricted = false))
        )
        assertThat(
            result.access.first { it.crn == PersonGenerator.DEFAULT.crn },
            equalTo(CaseAccess(PersonGenerator.DEFAULT.crn, userExcluded = false, userRestricted = false))
        )
    }

    @Test
    fun `get all access limitations`() {
        mockMvc.perform(get("/person/${PersonGenerator.RESTRICTION_EXCLUSION.crn}/limited-access/all").withToken())
            .andExpectJson(
                CaseAccessList(
                    crn = PersonGenerator.RESTRICTION_EXCLUSION.crn,
                    excludedFrom = listOf(User(UserGenerator.LIMITED_ACCESS_USER.username, null)),
                    restrictedTo = listOf(
                        User(
                            StaffGenerator.STAFF_WITH_USER.user!!.username,
                            StaffGenerator.STAFF_WITH_USER.code
                        )
                    ),
                    exclusionMessage = PersonGenerator.RESTRICTION_EXCLUSION.exclusionMessage,
                    restrictionMessage = PersonGenerator.RESTRICTION_EXCLUSION.restrictionMessage,
                )
            )
    }

    @Test
    fun `get all access limitations filtered by staff code`() {
        val staff = StaffGenerator.STAFF_WITH_USER
        mockMvc.perform(
            post("/person/${PersonGenerator.RESTRICTION_EXCLUSION.crn}/limited-access")
                .withToken()
                .withJson(listOf(staff.code))
        ).andExpectJson(
            CaseAccessList(
                crn = PersonGenerator.RESTRICTION_EXCLUSION.crn,
                excludedFrom = emptyList(),
                restrictedTo = listOf(User(staff.user!!.username, staff.code)),
                exclusionMessage = PersonGenerator.RESTRICTION_EXCLUSION.exclusionMessage,
                restrictionMessage = PersonGenerator.RESTRICTION_EXCLUSION.restrictionMessage,
            )
        )

        mockMvc.perform(
            post("/person/${PersonGenerator.RESTRICTION_EXCLUSION.crn}/limited-access")
                .withToken()
                .withJson(listOf("OTHER"))
        ).andExpectJson(
            CaseAccessList(
                crn = PersonGenerator.RESTRICTION_EXCLUSION.crn,
                excludedFrom = emptyList(),
                restrictedTo = emptyList(),
                exclusionMessage = PersonGenerator.RESTRICTION_EXCLUSION.exclusionMessage,
                restrictionMessage = PersonGenerator.RESTRICTION_EXCLUSION.restrictionMessage,
            )
        )
    }
}

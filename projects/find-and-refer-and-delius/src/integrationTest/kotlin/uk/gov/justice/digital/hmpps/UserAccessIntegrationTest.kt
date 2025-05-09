package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessUserGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserAccessIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `user is excluded from viewing a case`() {

        val exclusionUser = LimitedAccessUserGenerator.EXCLUSION_USER
        val exclusionPerson = PersonGenerator.EXCLUSION
        val response = mockMvc
            .perform(get("/users/${exclusionUser.username}/access/${exclusionPerson.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()

        assertThat(response.crn, equalTo(exclusionPerson.crn))
        assertThat(response.userExcluded, equalTo(true))
        assertThat(response.userRestricted, equalTo(false))
        assertThat(response.exclusionMessage, equalTo("There is an exclusion on this person"))
        assertThat(response.restrictionMessage, equalTo(null))
    }

    @Test
    fun `user is restricted from viewing a restricted case`() {

        val user = LimitedAccessUserGenerator.EXCLUSION_USER
        val restrictionPerson = PersonGenerator.RESTRICTION
        val response = mockMvc
            .perform(get("/users/${user.username}/access/${restrictionPerson.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()

        assertThat(response.crn, equalTo(restrictionPerson.crn))
        assertThat(response.userExcluded, equalTo(false))
        assertThat(response.userRestricted, equalTo(true))
        assertThat(response.exclusionMessage, equalTo(null))
        assertThat(response.restrictionMessage, equalTo("There is a restriction on this person"))
    }

    @Test
    fun `user is not restricted from viewing a restricted case`() {

        val restrictionUser = LimitedAccessUserGenerator.RESTRICTION_USER
        val restrictionPerson = PersonGenerator.RESTRICTION
        val response = mockMvc
            .perform(get("/users/${restrictionUser.username}/access/${restrictionPerson.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()

        assertThat(response.crn, equalTo(restrictionPerson.crn))
        assertThat(response.userExcluded, equalTo(false))
        assertThat(response.userRestricted, equalTo(false))
        assertThat(response.exclusionMessage, equalTo(null))
        assertThat(response.restrictionMessage, equalTo(null))
    }

    @Test
    fun `user is not restricted from viewing a restricted case but is excluded from viewing the case `() {

        val user = LimitedAccessUserGenerator.RESTRICTION_AND_EXCLUSION_USER
        val restrictionExclusionPerson = PersonGenerator.RESTRICTION_EXCLUSION
        val response = mockMvc
            .perform(get("/users/${user.username}/access/${restrictionExclusionPerson.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseAccess>()

        assertThat(response.crn, equalTo(restrictionExclusionPerson.crn))
        assertThat(response.userExcluded, equalTo(true))
        assertThat(response.userRestricted, equalTo(false))
        assertThat(response.exclusionMessage, equalTo("You are excluded from viewing this case"))
        assertThat(response.restrictionMessage, equalTo(null))
    }
}

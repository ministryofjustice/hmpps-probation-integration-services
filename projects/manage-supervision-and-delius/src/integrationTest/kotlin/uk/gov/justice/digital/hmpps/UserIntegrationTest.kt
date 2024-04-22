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
import uk.gov.justice.digital.hmpps.api.model.user.User
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.service.name

import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `all caseload activity is for a user`() {

        val person = USER
        val res = mockMvc
            .perform(get("/caseload/${person.username}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<User>()

        assertThat(res.cases[0].crn, equalTo(OVERVIEW.crn))
        assertThat(res.cases[0].caseName, equalTo(OVERVIEW.name()))
        assertThat(res.teams[0].description, equalTo(DEFAULT_TEAM.description))
        assertThat(res.teams[0].cases[0].crn, equalTo(OVERVIEW.crn))
        assertThat(res.teams[0].cases[0].caseName, equalTo(OVERVIEW.name()))
        assertThat(res.teams[0].cases[1].crn, equalTo(PERSONAL_DETAILS.crn))
        assertThat(res.teams[0].cases[1].caseName, equalTo(PERSONAL_DETAILS.name()))
    }

    @Test
    fun `all caseload activity is for another user`() {

        val person = USER_1
        val res = mockMvc
            .perform(get("/caseload/${person.username}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<User>()

        assertThat(res.cases[0].crn, equalTo(PERSONAL_DETAILS.crn))
        assertThat(res.cases[0].caseName, equalTo(PERSONAL_DETAILS.name()))
        assertThat(res.teams[0].description, equalTo(DEFAULT_TEAM.description))
        assertThat(res.teams[0].cases[1].crn, equalTo(PERSONAL_DETAILS.crn))
        assertThat(res.teams[0].cases[1].caseName, equalTo(PERSONAL_DETAILS.name()))
        assertThat(res.teams[0].cases[0].crn, equalTo(OVERVIEW.crn))
        assertThat(res.teams[0].cases[0].caseName, equalTo(OVERVIEW.name()))
    }

    @Test
    fun `not found status returned`() {
        mockMvc
            .perform(get("/caseload/invalidusername").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(get("/caseload/invalidusername"))
            .andExpect(status().isUnauthorized)
    }
}

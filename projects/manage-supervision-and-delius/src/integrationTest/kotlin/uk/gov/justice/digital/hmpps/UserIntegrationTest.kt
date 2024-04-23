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
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.user.StaffCaseload
import uk.gov.justice.digital.hmpps.api.model.user.TeamStaff
import uk.gov.justice.digital.hmpps.api.model.user.UserTeam
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.STAFF_1
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
            .perform(get("/caseload/user/${person.username}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.provider, equalTo(DEFAULT_PROVIDER.description))
        assertThat(res.caseload[0].crn, equalTo(OVERVIEW.crn))
        assertThat(res.caseload[0].caseName, equalTo(OVERVIEW.name()))
    }

    @Test
    fun `all caseload activity is for another user`() {

        val person = USER_1
        val res = mockMvc
            .perform(get("/caseload/user/${person.username}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.provider, equalTo(DEFAULT_PROVIDER.description))
        assertThat(res.caseload[0].crn, equalTo(PERSONAL_DETAILS.crn))
        assertThat(res.caseload[0].caseName, equalTo(PERSONAL_DETAILS.name()))
    }

    @Test
    fun `not found status returned`() {
        mockMvc
            .perform(get("/caseload/user/invalidusername").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(get("/caseload/user/invalidusername"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `teams for a user`() {

        val person = USER_1
        val res = mockMvc
            .perform(get("/caseload/user/${person.username}/teams").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<UserTeam>()

        assertThat(res.provider, equalTo(DEFAULT_PROVIDER.description))
        assertThat(res.teams[0].description, equalTo(DEFAULT_TEAM.description))
        assertThat(res.teams[0].code.trim(), equalTo(DEFAULT_TEAM.code.trim()))
    }

    @Test
    fun `staff for a team`() {

        val res = mockMvc
            .perform(get("/caseload/team/${DEFAULT_TEAM.code}/staff").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<TeamStaff>()

        assertThat(res.provider, equalTo(DEFAULT_PROVIDER.description))
        assertThat(res.staff[0].code.trim(), equalTo(DEFAULT_STAFF.code))
        assertThat(res.staff[0].name, equalTo(Name(forename = DEFAULT_STAFF.forename, surname = DEFAULT_STAFF.surname)))
        assertThat(res.staff[1].code.trim(), equalTo(STAFF_1.code))
        assertThat(res.staff[1].name, equalTo(Name(forename = STAFF_1.forename, surname = STAFF_1.surname)))
    }

    @Test
    fun `case load for a staffcode`() {

        val person = USER_1
        val res = mockMvc
            .perform(get("/caseload/staff/${person.staff?.code}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.provider, equalTo(DEFAULT_PROVIDER.description))
        assertThat(res.caseload[0].crn, equalTo(PERSONAL_DETAILS.crn))
        assertThat(res.caseload[0].caseName, equalTo(PERSONAL_DETAILS.name()))
    }
}

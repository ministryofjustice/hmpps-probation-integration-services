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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.user.*
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
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
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

        val code = DEFAULT_TEAM.code.trim()
        val res = mockMvc
            .perform(get("/caseload/team/${code}/staff").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<TeamStaff>()

        assertThat(res.provider, equalTo(DEFAULT_PROVIDER.description))
        assertThat(res.staff[0].code.trim(), equalTo(DEFAULT_STAFF.code.trim()))
        assertThat(res.staff[0].name, equalTo(Name(forename = DEFAULT_STAFF.forename, surname = DEFAULT_STAFF.surname)))
        assertThat(res.staff[1].code.trim(), equalTo(STAFF_1.code.trim()))
        assertThat(res.staff[1].name, equalTo(Name(forename = STAFF_1.forename, surname = STAFF_1.surname)))
    }

    @Test
    fun `all caseload activity for a team`() {

        val code = DEFAULT_TEAM.code.trim()
        val res = mockMvc
            .perform(get("/caseload/team/${code}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<TeamCaseload>()

        assertThat(res.provider, equalTo(DEFAULT_PROVIDER.description))
        assertThat(res.caseload[0].crn, equalTo(OVERVIEW.crn))
        assertThat(res.caseload[0].caseName, equalTo(OVERVIEW.name()))
        assertThat(res.caseload[1].crn, equalTo(PERSONAL_DETAILS.crn))
        assertThat(res.caseload[1].caseName, equalTo(PERSONAL_DETAILS.name()))
    }

    @Test
    fun `caseload search returns all when no search criteria or sort specified`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = null, sentenceCode = null))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.caseload.size, equalTo(2))
    }

    @Test
    fun `caseload search returns one when name part specified`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = "Blog", nextContactCode = null, sentenceCode = null))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.caseload.size, equalTo(1))
        assertThat(res.caseload[0].crn, equalTo("X000005"))
    }

    @Test
    fun `caseload search returns one case when 2 name parts specified`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search").withToken()
                    .withJson(
                        UserSearchFilter(
                            nameOrCrn = "Caroline Blog",
                            nextContactCode = null,
                            sentenceCode = null
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.caseload.size, equalTo(1))
        assertThat(res.caseload[0].crn, equalTo("X000005"))
    }

    @Test
    fun `caseload search returns 1 case when sentence specified in filter`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = null, sentenceCode = "DFS"))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.caseload.size, equalTo(1))
        assertThat(res.caseload[0].crn, equalTo("X000004"))
        assertThat(res.caseload[0].latestSentence, equalTo("Default Sentence Type"))
    }

    @Test
    fun `caseload search returns 1 case when nextAppointment type specified in filter`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = "CODI", sentenceCode = null))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.caseload.size, equalTo(1))
        assertThat(res.caseload[0].crn, equalTo("X000004"))
        assertThat(res.caseload[0].nextAppointment?.description, equalTo("Initial Appointment on Doorstep (NS)"))
    }

    @Test
    fun `caseload search returns null sentence type as last case when sentence type is in the sort criteria as descending`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search?sortBy=sentence.desc").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = null, sentenceCode = null))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.caseload.size, equalTo(2))
        assertThat(res.caseload[1].crn, equalTo("X000005"))
        assertThat(res.caseload[1].latestSentence, equalTo(null))
    }

    @Test
    fun `caseload search returns null next appointment as the last case when next contact is in the sort criteria as descending`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search?sortBy=nextContact.desc").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = null, sentenceCode = null))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.caseload.size, equalTo(2))
        assertThat(res.caseload[1].crn, equalTo("X000005"))
        assertThat(res.caseload[1].nextAppointment, equalTo(null))
    }

    @Test
    fun `caseload search returns crn with name bloggs first when sorting by surname asc`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search?sortBy=surname.asc").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = null, sentenceCode = null))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.caseload.size, equalTo(2))
        assertThat(res.caseload[0].crn, equalTo("X000005"))
        assertThat(res.caseload[0].caseName?.surname, equalTo("Bloggs"))
    }

    @Test
    fun `caseload search returns crn with name Surname first when sorting by surname desc`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search?sortBy=surname.desc").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = null, sentenceCode = null))
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        assertThat(res.caseload.size, equalTo(2))
        assertThat(res.caseload[0].crn, equalTo("X000004"))
        assertThat(res.caseload[0].caseName?.surname, equalTo("Surname"))
        assertThat(res.metaData?.contactTypes?.size, equalTo(1))
        assertThat(res.metaData?.sentenceTypes?.size, equalTo(1))
    }

    @Test
    fun `caseload search throws a bad request when the sortBy is not in the correct format`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search?sortBy=surname.desc.ssss").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = null, sentenceCode = null))
            )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()
        assertThat(res.message, equalTo("Sort criteria invalid format"))
    }

    @Test
    fun `caseload search throws a bad request when the sortBy is not implemented`() {

        val user = USER
        val res = mockMvc
            .perform(
                post("/caseload/user/${user.username}/search?sortBy=sausages.desc").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = null, sentenceCode = null))
            )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()
        assertThat(res.message, equalTo("Sort by sausages.desc is not implemented"))
    }

    @Test
    fun `caseload search throws a not found when the user is not known`() {
        mockMvc
            .perform(
                post("/caseload/user/NOT_KNOWN/search?sortBy=sentence.desc").withToken()
                    .withJson(UserSearchFilter(nameOrCrn = null, nextContactCode = null, sentenceCode = null))
            )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `caseload search throws a bad request when no request body is found`() {
        val user = USER
        mockMvc
            .perform(post("/caseload/user/${user.username}/search?sortBy=sentence.desc").withToken())
            .andExpect(status().isBadRequest)
    }
}

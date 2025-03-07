package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.user.StaffCaseload
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.LIMITED_ACCESS_USER
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.EXCLUSION
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.RESTRICTION
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.RESTRICTION_EXCLUSION
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class LaoCaseloadIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `all caseload activity for an lao user`() {
        val person = LIMITED_ACCESS_USER
        val res = mockMvc
            .perform(get("/caseload/user/${person.username}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<StaffCaseload>()

        val caseload = res.caseload.sortedBy { it.crn }

        assertThat(caseload[0].crn, equalTo(RESTRICTION_EXCLUSION.crn))
        assertThat(caseload[1].crn, equalTo(EXCLUSION.crn))
        assertThat(caseload[2].crn, equalTo(RESTRICTION.crn))


        assertThat(caseload[0].limitedAccess, equalTo(true))
        assertThat(caseload[0].caseName, equalTo(null))
        assertThat(caseload[0].latestSentence, equalTo(null))
        assertThat(caseload[0].nextAppointment, equalTo(null))
        assertThat(caseload[0].previousAppointment, equalTo(null))
        assertThat(caseload[0].numberOfAdditionalSentences, equalTo(null))

        assertThat(caseload[1].limitedAccess, equalTo(true))
        assertThat(caseload[1].caseName, equalTo(null))
        assertThat(caseload[1].latestSentence, equalTo(null))
        assertThat(caseload[1].nextAppointment, equalTo(null))
        assertThat(caseload[1].previousAppointment, equalTo(null))
        assertThat(caseload[1].numberOfAdditionalSentences, equalTo(null))

        assertThat(caseload[2].limitedAccess, equalTo(true))
        assertThat(caseload[2].caseName, equalTo(null))
        assertThat(caseload[2].latestSentence, equalTo(null))
        assertThat(caseload[2].nextAppointment, equalTo(null))
        assertThat(caseload[2].previousAppointment, equalTo(null))
        assertThat(caseload[2].numberOfAdditionalSentences, equalTo(null))

        assertThat(caseload[3].limitedAccess, equalTo(false))
        assertNotEquals(caseload[3].caseName, null)
    }

    @Test
    fun `check lao access for a user with list of crns`() {
        val person = LIMITED_ACCESS_USER
        val crns = listOf(RESTRICTION_EXCLUSION.crn, EXCLUSION.crn, RESTRICTION.crn, PERSONAL_DETAILS.crn)
        val res = mockMvc
            .perform(
                MockMvcRequestBuilders.post("/user/${person.username}/access").withToken()
                    .withJson(crns)
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<UserAccess>()

        val userAccess = res.access.sortedBy { it.crn }

        assertThat(userAccess[0].userExcluded, equalTo(true))
        assertThat(userAccess[0].userRestricted, equalTo(true))

        assertThat(userAccess[1].userExcluded, equalTo(true))
        assertThat(userAccess[1].userRestricted, equalTo(false))

        assertThat(userAccess[2].userExcluded, equalTo(false))
        assertThat(userAccess[2].userRestricted, equalTo(true))

        assertThat(userAccess[3].userExcluded, equalTo(false))
        assertThat(userAccess[3].userRestricted, equalTo(false))
    }

    @Test
    fun `check lao access returns 400 when no crns are provided`() {
        val person = LIMITED_ACCESS_USER
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/user/${person.username}/access").withToken()
                    .withJson(emptyList<String>())
            )
            .andExpect(status().isBadRequest)
    }
}

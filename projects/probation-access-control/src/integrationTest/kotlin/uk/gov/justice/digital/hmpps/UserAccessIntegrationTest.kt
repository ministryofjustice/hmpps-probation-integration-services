package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.service.AllCaseAccess
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserAccessIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val wireMockServer: WireMockServer
) {

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `can retrieve user exclusion details for a single crn`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.EXCLUDED
        val response = mockMvc.get("/user/${user.username}/access/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseAccess>()

        assertThat(
            response,
            equalTo(
                CaseAccess(
                    person.crn,
                    userExcluded = true,
                    userRestricted = false,
                    exclusionMessage = person.exclusionMessage
                )
            )
        )
    }

    @Test
    fun `can retrieve user restriction details for a single crn`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.RESTRICTED
        val response = mockMvc.get("/user/${user.username}/access/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseAccess>()

        assertThat(
            response,
            equalTo(
                CaseAccess(
                    person.crn,
                    userExcluded = false,
                    userRestricted = true,
                    restrictionMessage = person.restrictionMessage
                )
            )
        )
    }

    @Test
    fun `can retrieve user exclusion and restriction details for a single crn`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.BOTH
        val response = mockMvc.get("/user/${user.username}/access/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseAccess>()

        assertThat(
            response,
            equalTo(
                CaseAccess(
                    person.crn,
                    userExcluded = true,
                    userRestricted = true,
                    exclusionMessage = person.exclusionMessage,
                    restrictionMessage = person.restrictionMessage
                )
            )
        )
    }

    @Test
    fun `can retrieve user details without excluson or restriction`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT
        val response = mockMvc.get("/user/${user.username}/access/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseAccess>()

        assertThat(
            response,
            equalTo(CaseAccess(person.crn, userExcluded = false, userRestricted = false))
        )
    }

    @Test
    fun `can retrieve user access details for multiple crns`() {
        val user = UserGenerator.DEFAULT
        val personExcluded = PersonGenerator.EXCLUDED
        val personRestricted = PersonGenerator.RESTRICTED
        val personNoLAO = PersonGenerator.DEFAULT

        val crns = listOf(personExcluded.crn, personRestricted.crn, personNoLAO.crn)

        val response = mockMvc.post("/user/${user.username}/access") {
            json = crns
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<UserAccess>()

        assertThat(response.access.size, equalTo(3))
        assertThat(
            response.access.first { it.crn == personExcluded.crn },
            equalTo(
                CaseAccess(
                    personExcluded.crn,
                    userExcluded = true,
                    userRestricted = false,
                    exclusionMessage = personExcluded.exclusionMessage,
                    restrictionMessage = null
                )
            )
        )
        assertThat(
            response.access.first { it.crn == personRestricted.crn },
            equalTo(
                CaseAccess(
                    personRestricted.crn,
                    userExcluded = false,
                    userRestricted = true,
                    exclusionMessage = null,
                    restrictionMessage = personRestricted.restrictionMessage
                )
            )
        )
        assertThat(
            response.access.first { it.crn == personNoLAO.crn },
            equalTo(
                CaseAccess(
                    personNoLAO.crn,
                    userExcluded = false,
                    userRestricted = false,
                    exclusionMessage = null,
                    restrictionMessage = null
                )
            )
        )
    }

    @Test
    fun `can retrieve all exclusions for an excluded case`() {
        val person = PersonGenerator.EXCLUDED
        val response = mockMvc.get("/case/${person.crn}/access") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<AllCaseAccess>()

        assertThat(response.crn, equalTo(person.crn))
        assertThat(response.excludedFrom!!.size, equalTo(1))
        assertThat(response.excludedFrom!![0].username, equalTo(UserGenerator.DEFAULT.username))
        assertThat(response.restrictedTo, equalTo(null))
        assertThat(response.exclusionMessage, equalTo(person.exclusionMessage))
        assertThat(response.restrictionMessage, equalTo(null))
    }

    @Test
    fun `can retrieve all restrictions for a restricted case`() {
        val person = PersonGenerator.RESTRICTED
        val response = mockMvc.get("/case/${person.crn}/access") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<AllCaseAccess>()

        assertThat(response.crn, equalTo(person.crn))
        assertThat(response.excludedFrom, equalTo(null))
        assertThat(response.restrictedTo!!.size, equalTo(1))
        assertThat(response.restrictedTo!![0].username, equalTo(UserGenerator.RESTRICTED.username))
        assertThat(response.restrictionMessage, equalTo(person.restrictionMessage))
        assertThat(response.exclusionMessage, equalTo(null))
    }

    @Test
    fun `can retrieve all exclusions and restrictions for a case with both`() {
        val person = PersonGenerator.BOTH
        val response = mockMvc.get("/case/${person.crn}/access") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<AllCaseAccess>()

        assertThat(response.crn, equalTo(person.crn))
        assertThat(response.excludedFrom!!.size, equalTo(1))
        assertThat(response.excludedFrom!![0].username, equalTo(UserGenerator.DEFAULT.username))
        assertThat(response.restrictedTo!!.size, equalTo(1))
        assertThat(response.restrictedTo!![0].username, equalTo(UserGenerator.RESTRICTED.username))
        assertThat(response.exclusionMessage, equalTo(person.exclusionMessage))
        assertThat(response.restrictionMessage, equalTo(person.restrictionMessage))
    }

    @Test
    fun `returns null lists for a case with no exclusions or restrictions`() {
        val person = PersonGenerator.DEFAULT
        val response = mockMvc.get("/case/${person.crn}/access") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<AllCaseAccess>()

        assertThat(response.crn, equalTo(person.crn))
        assertThat(response.excludedFrom, equalTo(null))
        assertThat(response.restrictedTo, equalTo(null))
        assertThat(response.exclusionMessage, equalTo(null))
        assertThat(response.restrictionMessage, equalTo(null))
    }
}

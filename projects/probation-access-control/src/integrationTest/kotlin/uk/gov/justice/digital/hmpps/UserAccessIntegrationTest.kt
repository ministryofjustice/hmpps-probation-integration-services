package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.LimitedAccessDetail
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

    @Test
    fun `can retrieve all case access details`() {
        val response = mockMvc.get("/all-cases?page=0&size=10") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
            }
            .andReturn().response.contentAsJson<AllCasesResponse>()

        assertThat(response.content, hasSize(4))
        assertThat(response.page.size, equalTo(10L))
        assertThat(response.page.number, equalTo(0L))
        assertThat(response.page.totalElements, equalTo(4L))
        assertThat(response.page.totalPages, equalTo(1L))

        val excluded = response.content.first { it.crn == "E123456" }
        assertThat(excluded.username, equalTo(UserGenerator.DEFAULT.username))
        assertThat(excluded.type, equalTo("Exclusion"))
        assertThat(excluded.exclusionMessage, equalTo(PersonGenerator.EXCLUDED.exclusionMessage))
        assertThat(excluded.restrictionMessage, equalTo(null))
        assertThat(excluded.startDate, notNullValue())

        val restricted = response.content.first { it.crn == "R123456" }
        assertThat(restricted.username, equalTo(UserGenerator.RESTRICTED.username))
        assertThat(restricted.type, equalTo("Restriction"))
        assertThat(restricted.exclusionMessage, equalTo(null))
        assertThat(restricted.restrictionMessage, equalTo(PersonGenerator.RESTRICTED.restrictionMessage))
        assertThat(restricted.startDate, notNullValue())

        val bothExclusion = response.content.first { it.crn == "B123456" && it.type == "Exclusion" }
        val bothRestriction = response.content.first { it.crn == "B123456" && it.type == "Restriction" }

        assertThat(bothExclusion.username, equalTo(UserGenerator.DEFAULT.username))
        assertThat(bothRestriction.username, equalTo(UserGenerator.RESTRICTED.username))
        assertThat(bothExclusion.exclusionMessage, equalTo(PersonGenerator.BOTH.exclusionMessage))
        assertThat(bothRestriction.restrictionMessage, equalTo(PersonGenerator.BOTH.restrictionMessage))
    }

    @Test
    fun `all cases rejects negative page`() {
        val response = mockMvc.get("/all-cases?page=-1&size=10") { withToken() }
            .andExpect {
                status { isBadRequest() }
            }
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.status, equalTo(400))
        assertThat(response.message, equalTo("page must be >= 0"))
    }

    @Test
    fun `all cases rejects size outside allowed range`() {
        val tooSmall = mockMvc.get("/all-cases?page=0&size=0") { withToken() }
            .andExpect {
                status { isBadRequest() }
            }
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(tooSmall.status, equalTo(400))
        assertThat(tooSmall.message, equalTo("size must be between 1 and 1000"))

        val tooLarge = mockMvc.get("/all-cases?page=0&size=1001") { withToken() }
            .andExpect {
                status { isBadRequest() }
            }
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(tooLarge.status, equalTo(400))
        assertThat(tooLarge.message, equalTo("size must be between 1 and 1000"))
    }

    private data class AllCasesResponse(
        val content: List<LimitedAccessDetail>,
        val page: PageMetadata,
    )

    private data class PageMetadata(
        val size: Long,
        val number: Long,
        val totalElements: Long,
        val totalPages: Long,
    )
}

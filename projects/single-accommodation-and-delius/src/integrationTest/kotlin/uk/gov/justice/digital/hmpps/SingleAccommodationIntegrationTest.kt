package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class SingleAccommodationIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val wireMockServer: WireMockServer,
) {

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `can retrieve case list for user's team`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT

        val response = mockMvc.get("/case-list/${user.username}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseListResponse>()

        assertThat(response.cases.size).isEqualTo(4)
        assertThat(response.page.totalElements).isEqualTo(4)
        assertThat(response.page.totalPages).isEqualTo(1)
        assertThat(response.page.number).isEqualTo(0)
        assertThat(response.page.size).isEqualTo(50)
        assertThat(response.cases.any { it.crn == PersonGenerator.TEAM.crn && it.staff.code == StaffGenerator.TEAM_STAFF.code }).isTrue()
        assertThat(response.cases.none { it.crn == PersonGenerator.OTHER_TEAM.crn }).isTrue()
        val defaultCase = response.cases.first { it.crn == person.crn }
        assertThat(defaultCase).isEqualTo(
            Case(
                crn = person.crn,
                name = Name(
                    forename = person.firstName,
                    middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" ").ifEmpty { null },
                    surname = person.surname
                ),
                nomsNumber = person.noms,
                pncNumber = person.pnc,
                dateOfBirth = person.dateOfBirth,
                staff = Officer(
                    name = Name(
                        forename = staff.forename,
                        middleName = staff.middleName,
                        surname = staff.surname
                    ),
                    username = user.username,
                    code = staff.code
                ),
                team = CodeDescription(code = team.code, description = team.description),
                gender = person.gender.description,
                roshLevel = CodeDescription("RHRH", "High RoSH"),
                expectedReleaseDate = KeyDateGenerator.EXPECTED_RELEASE.date,
                userExcluded = false,
                userRestricted = false,
                exclusionMessage = null,
                restrictionMessage = null,
                isLimitedAccess = false
            )
        )
    }

    @Test
    fun `can retrieve case list for all of user's teams`() {
        val user = UserGenerator.OTHER
        val otherTeamExcludedPerson = PersonGenerator.EXCLUDED
        val otherTeamRestrictedPerson = PersonGenerator.RESTRICTED

        val response = mockMvc.get("/case-list/${user.username}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseListResponse>()

        assertThat(response.cases.size).isEqualTo(5)

        val otherTeamExcludedCase = response.cases.single { it.crn == otherTeamExcludedPerson.crn }
        assertThat(otherTeamExcludedCase.userExcluded).isFalse()
        assertThat(otherTeamExcludedCase.userRestricted).isFalse()
        assertThat(otherTeamExcludedCase.isLimitedAccess).isTrue()

        val otherTeamRestrictedCase = response.cases.single { it.crn == otherTeamRestrictedPerson.crn }
        assertThat(otherTeamRestrictedCase.userExcluded).isFalse()
        assertThat(otherTeamRestrictedCase.userRestricted).isTrue()
        assertThat(otherTeamRestrictedCase.isLimitedAccess).isTrue()
    }

    @Test
    fun `can paginate case list`() {
        val user = UserGenerator.DEFAULT

        val firstPage = mockMvc.get("/case-list/${user.username}?page=0&size=2") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseListResponse>()

        assertThat(firstPage.cases.size).isEqualTo(2)
        assertThat(firstPage.page.totalElements).isEqualTo(4)
        assertThat(firstPage.page.totalPages).isEqualTo(2)
        assertThat(firstPage.page.number).isEqualTo(0)
        assertThat(firstPage.page.size).isEqualTo(2)

        val secondPage = mockMvc.get("/case-list/${user.username}?page=1&size=2") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseListResponse>()

        assertThat(secondPage.cases.size).isEqualTo(2)
        assertThat(secondPage.page.totalElements).isEqualTo(4)
        assertThat(secondPage.page.totalPages).isEqualTo(2)
        assertThat(secondPage.page.number).isEqualTo(1)
        assertThat(secondPage.page.size).isEqualTo(2)
    }

    @Test
    fun `can retrieve case for user crn`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT

        val response = mockMvc.get("/case/${user.username}/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<Case>()

        assertThat(response).isEqualTo(
            Case(
                crn = person.crn,
                name = Name(
                    forename = person.firstName,
                    middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" ").ifEmpty { null },
                    surname = person.surname
                ),
                nomsNumber = person.noms,
                pncNumber = person.pnc,
                dateOfBirth = person.dateOfBirth,
                staff = Officer(
                    name = Name(
                        forename = staff.forename,
                        middleName = staff.middleName,
                        surname = staff.surname
                    ),
                    username = UserGenerator.DEFAULT.username,
                    code = staff.code
                ),
                team = CodeDescription(code = team.code, description = team.description),
                gender = person.gender.description,
                roshLevel = CodeDescription("RHRH", "High RoSH"),
                expectedReleaseDate = KeyDateGenerator.EXPECTED_RELEASE.date,
                userExcluded = false,
                userRestricted = false,
                exclusionMessage = null,
                restrictionMessage = null,
                isLimitedAccess = false,
            )
        )
    }

    @Test
    fun `can retrieve excluded case for user crn with LAO fields`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.EXCLUDED

        val response = mockMvc.get("/case/${user.username}/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<Case>()


        assertThat(response.userExcluded).isTrue()
        assertThat(response.userRestricted).isFalse()
        assertThat(response.exclusionMessage).isNotNull()
        assertThat(response.restrictionMessage).isNull()
        assertThat(response.isLimitedAccess).isTrue()
    }

    @Test
    fun `can retrieve restricted case for user crn with LAO fields`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.RESTRICTED

        val response = mockMvc.get("/case/${user.username}/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<Case>()

        assertThat(response.userExcluded).isFalse()
        assertThat(response.userRestricted).isTrue()
        assertThat(response.exclusionMessage).isNull()
        assertThat(response.restrictionMessage).isNotNull()
        assertThat(response.isLimitedAccess).isTrue()
    }

    @Test
    fun `can't retrieve case for non existent user crn`() {
        val user = UserGenerator.DEFAULT

        mockMvc.get("/case/${user.username}/A999999") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `can't retrieve case without token`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT

        mockMvc.get("/case/${user.username}/${person.crn}")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `excluded case returns LAO fields`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.EXCLUDED
        val staff = StaffGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT

        val response = mockMvc.get("/case-list/${user.username}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseListResponse>()

        val excludedCase = response.cases.first { it.crn == person.crn }
        assertThat(excludedCase).isEqualTo(
            Case(
                crn = person.crn,
                name = Name(
                    forename = person.firstName,
                    middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" ").ifEmpty { null },
                    surname = person.surname
                ),
                nomsNumber = person.noms,
                pncNumber = person.pnc,
                dateOfBirth = person.dateOfBirth,
                staff = Officer(
                    name = Name(
                        forename = staff.forename,
                        middleName = staff.middleName,
                        surname = staff.surname
                    ),
                    username = user.username,
                    code = staff.code
                ),
                team = CodeDescription(code = team.code, description = team.description),
                gender = person.gender.description,
                roshLevel = null,
                expectedReleaseDate = null,
                userExcluded = true,
                userRestricted = false,
                exclusionMessage = LimitedAccessGenerator.EXCLUDED_CASE.exclusionMessage,
                restrictionMessage = null,
                isLimitedAccess = true,
            )
        )
    }

    @Test
    fun `restricted case returns LAO fields`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.RESTRICTED
        val staff = StaffGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT

        val response = mockMvc.get("/case-list/${user.username}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseListResponse>()

        val restrictedCase = response.cases.first { it.crn == person.crn }
        assertThat(restrictedCase).isEqualTo(
            Case(
                crn = person.crn,
                name = Name(
                    forename = person.firstName,
                    middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" ").ifEmpty { null },
                    surname = person.surname
                ),
                nomsNumber = person.noms,
                pncNumber = person.pnc,
                dateOfBirth = person.dateOfBirth,
                staff = Officer(
                    name = Name(
                        forename = staff.forename,
                        middleName = staff.middleName,
                        surname = staff.surname
                    ),
                    username = user.username,
                    code = staff.code
                ),
                team = CodeDescription(code = team.code, description = team.description),
                gender = person.gender.description,
                roshLevel = null,
                expectedReleaseDate = null,
                userExcluded = false,
                userRestricted = true,
                exclusionMessage = null,
                restrictionMessage = LimitedAccessGenerator.RESTRICTED_CASE.restrictionMessage,
                isLimitedAccess = true,
            )
        )
    }
}

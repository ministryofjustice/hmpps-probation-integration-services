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
    fun `can retrieve case list for user`() {
        val user = UserGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT

        val response = mockMvc.get("/case-list/${user.username}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseListResponse>()

        assertThat(response.cases.size).isEqualTo(3)
        val defaultCase = response.cases.first { it.crn == person.crn }
        assertThat(defaultCase).isEqualTo(
            Case(
                crn = person.crn,
                name = Name(
                    forename = person.firstName,
                    middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
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
                restrictionMessage = null
            )
        )
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
                    middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
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
                restrictionMessage = null
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
                    middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
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
                restrictionMessage = LimitedAccessGenerator.RESTRICTED_CASE.restrictionMessage
            )
        )
    }
}

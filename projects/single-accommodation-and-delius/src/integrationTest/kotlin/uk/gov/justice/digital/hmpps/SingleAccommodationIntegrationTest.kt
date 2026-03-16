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

        assertThat(response).isEqualTo(
            CaseListResponse(
                cases = listOf(
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
                        roshLevel = "RHRH",
                        tier = "C1",
                        expectedReleaseDate = KeyDateGenerator.EXPECTED_RELEASE.date
                    )
                )
            )
        )
    }
}

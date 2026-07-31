package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.controller.model.HomeOfficeInterest
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class HomeOfficeInterestIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `get home office interest returns registration details`() {
        val crn = PersonGenerator.PERSON1.crn
        mockMvc.get("/case/$crn/home-office-interest") { withToken() }
            .andExpect { status { isOk() } }
            .andExpectJson(
                HomeOfficeInterest(
                    exists = true,
                    notes = RegistrationGenerator.HOME_OFFICE_INTEREST.notes
                )
            )
    }

    @Test
    fun `get home office interest returns 404 when no person found`() {
        mockMvc.get("/case/Z999999/home-office-interest") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get home office interest returns exists false when no registration`() {
        val crn = PersonGenerator.PERSON2.crn
        mockMvc.get("/case/$crn/home-office-interest") { withToken() }
            .andExpect { status { isOk() } }
            .andExpectJson(
                HomeOfficeInterest(
                    exists = false,
                    notes = null
                )
            )
    }

    @Test
    fun `get home office interest returns 401 without token`() {
        val crn = PersonGenerator.PERSON1.crn
        mockMvc.get("/case/$crn/home-office-interest")
            .andExpect { status { isUnauthorized() } }
    }
}

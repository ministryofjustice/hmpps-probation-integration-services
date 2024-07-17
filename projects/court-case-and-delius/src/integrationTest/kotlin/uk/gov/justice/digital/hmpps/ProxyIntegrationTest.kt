package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.isEqualTo
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.api.model.ProbationStatus
import uk.gov.justice.digital.hmpps.api.model.ProbationStatusDetail
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProxyIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `proxies to community api for offenders all`() {
        mockMvc
            .perform(get("/secure/offenders/crn/X320741/all").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.otherIds.crn").value("X320741"))
    }

    @Test
    fun `proxies to community api for offenders summary`() {
        mockMvc
            .perform(get("/secure/offenders/crn/X320741").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.contactDetails.addresses[0].town").value("Leicester"))
    }

    @Test
    fun `proxies to community api with error`() {
        mockMvc
            .perform(get("/secure/offenders/crn/CRNXXX/all").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.developerMessage").value("Offender with CRN 'CRNXXX' not found"))
    }
}

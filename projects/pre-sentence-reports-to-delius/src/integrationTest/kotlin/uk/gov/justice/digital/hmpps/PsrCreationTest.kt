package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PsrCreationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Test
    fun `create PSR unauthorised`() {
        val courtReport = CourtReportGenerator.DEFAULT
        mockMvc.perform(
            MockMvcRequestBuilders.post("/probation-cases/${courtReport.person.crn}/court-reports/${courtReport.id}/pre-sentence-reports")
                .content("{}")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `create PSR authorised`() {
        val courtReport = CourtReportGenerator.DEFAULT
        mockMvc.perform(
            MockMvcRequestBuilders.post("/probation-cases/${courtReport.person.crn}/court-reports/${courtReport.id}/pre-sentence-reports")
                .withOAuth2Token(wireMockserver)
                .contentType("application/json")
                .content("{\"type\": \"cr-type\"}")
        ).andExpect(status().isCreated)
    }
}

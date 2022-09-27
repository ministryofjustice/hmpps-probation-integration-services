package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReportRepository
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.Address
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.Court
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.LocalJusticeArea
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.Name
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportContext
import java.time.LocalDate
import java.util.UUID

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PsrContextIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @MockBean
    lateinit var courtReportRepository: CourtReportRepository

    @Test
    fun `get PSR Context unauthorised`() {
        val courtReportId: Long = CourtReportGenerator.DEFAULT.id
        mockMvc.perform(get("/context/$courtReportId"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get PSR Context not found`() {

        val reportId = UUID.randomUUID().toString()
        whenever(courtReportRepository.getCourtReportContextJson(reportId))
            .thenReturn(null)

        mockMvc.perform(
            get("/context/$reportId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${getToken()}")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get PSR Context`() {

        val reportId = UUID.randomUUID().toString()
        whenever(courtReportRepository.getCourtReportContextJson(reportId))
            .thenReturn(objectMapper.writeValueAsString(getPreSentenceReportContext()))

        val result = mockMvc.perform(
            get("/context/$reportId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${getToken()}")
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn()
        val detail = objectMapper.readValue(result.response.contentAsString, PreSentenceReportContext::class.java)

        assertThat(detail, equalTo(getPreSentenceReportContext()))
    }

    private fun getToken(): String {
        val authResponse = RestTemplate().postForObject(
            "http://localhost:${wireMockserver.port()}/auth/oauth/token",
            null,
            JsonNode::class.java
        )!!
        return authResponse["access_token"].asText()
    }

    private fun getPreSentenceReportContext(): PreSentenceReportContext {

        return PreSentenceReportContext(
            "X123456",
            Name("John", "Smith", "Hannibal"),
            LocalDate.of(2000, 1, 20),
            "PNC1111",
            Address(false, "building name", "123", "StreetName", "Town", "District", "County", "NE1 2SW"),
            Offence("MainOffence"),
            listOf(Offence("other offence")),
            Court("CourtName", LocalJusticeArea("Local justice area"))
        )
    }
}

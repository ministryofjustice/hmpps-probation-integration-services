package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.model.RoshSummaryDetails
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class RoshSummaryDetailsTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return rosh summary details`() {
        val result = mockMvc
            .perform(get("/rosh-summary/D006296").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
            .andReturn()

        val roshSummaryDetails = objectMapper.readValue(result.response.contentAsString, RoshSummaryDetails::class.java)
        assertThat(roshSummaryDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-09T14:33:53Z").withZoneSameInstant(EuropeLondon))
        assertThat(roshSummaryDetails.roshSummary.whoIsAtRisk)
            .isEqualTo("Various people in the community are at risk")
        assertThat(roshSummaryDetails.roshSummary.riskReductionLikelyTo)
            .isEqualTo("Alcohol monitoring and counselling course to reduce.\r\nThis has worked in the past")
        assertThat(roshSummaryDetails.roshSummary.riskIncreaseLikelyTo)
            .isEqualTo("Drinking increases risk")
        assertThat(roshSummaryDetails.roshSummary.riskGreatest)
            .isEqualTo("Risk greatest around Christmas")
        assertThat(roshSummaryDetails.roshSummary.natureOfRisk)
            .isEqualTo("Risks varying in nature")
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc
            .perform(get("/rosh-summary/D000001").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }
}

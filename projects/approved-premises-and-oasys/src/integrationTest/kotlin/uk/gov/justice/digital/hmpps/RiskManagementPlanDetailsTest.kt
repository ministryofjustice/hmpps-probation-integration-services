package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.model.RiskManagementPlanDetails
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class RiskManagementPlanDetailsTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return risk management plan details`() {
        val result = mockMvc
            .perform(get("/risk-management-plan/D006296").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
            .andReturn()

        val riskManagementPlanDetails =
            objectMapper.readValue(result.response.contentAsString, RiskManagementPlanDetails::class.java)
        assertThat(riskManagementPlanDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-09T14:33:53Z").withZoneSameInstant(EuropeLondon))
        assertThat(riskManagementPlanDetails.lastUpdatedDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-17T15:02:17Z").withZoneSameInstant(EuropeLondon))
        assertThat(riskManagementPlanDetails.riskManagementPlan.victimSafetyPlanning)
            .isEqualTo("tagging and Alcohol monitoring")
        assertThat(riskManagementPlanDetails.riskManagementPlan.monitoringAndControl)
            .isEqualTo("Monitored and Controlled by us")
        assertThat(riskManagementPlanDetails.riskManagementPlan.furtherConsiderations)
            .isEqualTo("Consider High Risk to Children")
        assertThat(riskManagementPlanDetails.riskManagementPlan.keyInformationAboutCurrentSituation)
            .isEqualTo("Paul Grimes is currently in the community having received a CJA2003 - Community Order on the 12/12/2021.\r\rThe end of their sentence is currently unknown. \r\rThey have accommodation and education, training & employability linked to risk.\r\rPaul Grimes has been assessed as high risk to children, staff and prisoners and medium risk to a known adult.\r\rThey are very motivated to address offending behaviour.\r")
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc
            .perform(get("/risk-management-plan/D000001").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }
}

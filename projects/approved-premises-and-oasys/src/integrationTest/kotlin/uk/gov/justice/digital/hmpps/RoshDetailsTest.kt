package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.model.RiskLevel
import uk.gov.justice.digital.hmpps.model.RoshDetails
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoshDetailsTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return rosh details`() {
        val result = mockMvc
            .perform(MockMvcRequestBuilders.get("/rosh/D006296").withOAuth2Token(wireMockServer))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn()

        val roshDetails = objectMapper.readValue(result.response.contentAsString, RoshDetails::class.java)
        assertThat(roshDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-09T14:33:53Z").withZoneSameInstant(EuropeLondon))
        assertThat(roshDetails.lastUpdatedDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-17T15:02:17Z").withZoneSameInstant(EuropeLondon))
        assertThat(roshDetails.rosh.riskChildrenCommunity)
            .isEqualTo(RiskLevel.HIGH)
        assertThat(roshDetails.rosh.riskPrisonersCustody)
            .isEqualTo(RiskLevel.HIGH)
        assertThat(roshDetails.rosh.riskStaffCustody)
            .isEqualTo(RiskLevel.LOW)
        assertThat(roshDetails.rosh.riskStaffCommunity)
            .isEqualTo(RiskLevel.HIGH)
        assertThat(roshDetails.rosh.riskKnownAdultCustody)
            .isEqualTo(RiskLevel.LOW)
        assertThat(roshDetails.rosh.riskKnownAdultCommunity)
            .isEqualTo(RiskLevel.MEDIUM)
        assertThat(roshDetails.rosh.riskPublicCustody)
            .isEqualTo(RiskLevel.LOW)
        assertThat(roshDetails.rosh.riskPublicCommunity)
            .isEqualTo(RiskLevel.LOW)
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/rosh/D000001").withOAuth2Token(wireMockServer))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}

package uk.gov.justice.digital.hmpps

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
import uk.gov.justice.digital.hmpps.model.RiskToTheIndividualDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class RiskToTheIndividualDetailsTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `should return risk to the individual details`() {
        val riskToTheIndividualDetails = mockMvc
            .perform(get("/risk-to-the-individual/D006296").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<RiskToTheIndividualDetails>()

        assertThat(riskToTheIndividualDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-09T14:33:53Z").withZoneSameInstant(EuropeLondon))
        assertThat(riskToTheIndividualDetails.lastUpdatedDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-17T15:02:17Z").withZoneSameInstant(EuropeLondon))
        assertThat(riskToTheIndividualDetails.riskToTheIndividual.concernsRiskOfSuicide)
            .isTrue
        assertThat(riskToTheIndividualDetails.riskToTheIndividual.currentConcernsSelfHarmSuicide)
            .isEqualTo("High Risk of Suicide\r\nHigh Risk of Self-harm")
        assertThat(riskToTheIndividualDetails.riskToTheIndividual.concernsRiskOfSelfHarm)
            .isTrue
        assertThat(riskToTheIndividualDetails.riskToTheIndividual.riskOfSeriousHarm)
            .isEqualTo("R8.4.1 RoSH")
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc
            .perform(get("/risk-to-the-individual/D000001").withToken())
            .andExpect(status().isNotFound)
    }
}

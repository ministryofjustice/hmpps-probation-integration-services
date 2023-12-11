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
import uk.gov.justice.digital.hmpps.model.RiskAssessmentDetails
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class RiskAssessmentDetailsTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return risk assessment details`() {
        val result = mockMvc
            .perform(get("/risk-assessment/D006296").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
            .andReturn()

        val riskToTheIndividualDetails =
            objectMapper.readValue(result.response.contentAsString, RiskAssessmentDetails::class.java)
        assertThat(riskToTheIndividualDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-09T14:33:53Z").withZoneSameInstant(EuropeLondon))
        assertThat(riskToTheIndividualDetails.lastUpdatedDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-17T15:02:17Z").withZoneSameInstant(EuropeLondon))
        assertThat(riskToTheIndividualDetails.riskAssessment.currentOffenceDetails)
            .isEqualTo("fight")
        assertThat(riskToTheIndividualDetails.riskAssessment.previousWhereAndWhen)
            .isEqualTo("in the library")
        assertThat(riskToTheIndividualDetails.riskAssessment.previousWhatDone)
            .isEqualTo("Stabbing")
        assertThat(riskToTheIndividualDetails.riskAssessment.previousWhyDone)
            .isEqualTo("revenge")
        assertThat(riskToTheIndividualDetails.riskAssessment.previousAnyoneElsePresent)
            .isEqualTo("Miss Scarlett")
        assertThat(riskToTheIndividualDetails.riskAssessment.previousWhoVictims)
            .isEqualTo("Mr White")
        assertThat(riskToTheIndividualDetails.riskAssessment.currentSources)
            .isEqualTo("witnesses")
        assertThat(riskToTheIndividualDetails.riskAssessment.currentWhyDone)
            .isEqualTo("for money")
        assertThat(riskToTheIndividualDetails.riskAssessment.currentAnyoneElsePresent)
            .isEqualTo("gang of people")
        assertThat(riskToTheIndividualDetails.riskAssessment.currentWhoVictims)
            .isEqualTo("passers by")
        assertThat(riskToTheIndividualDetails.riskAssessment.currentHowDone)
            .isEqualTo("knife")
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc
            .perform(get("/risk-assessment/D000001").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }
}

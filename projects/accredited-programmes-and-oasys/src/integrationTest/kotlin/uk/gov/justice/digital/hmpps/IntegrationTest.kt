package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.JsonNode
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.controller.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.math.BigDecimal
import java.time.LocalDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `get timeline returns ok`() {
        val json = mockMvc
            .perform(get("/assessments/timeline/A1234YZ").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<Timeline>()

        assertThat(
            json, equalTo(
                Timeline(
                    "T123456",
                    "A1234YZ",
                    listOf(AssessmentSummary(90123456, LocalDateTime.of(2024, 1, 15, 11, 30), "LAYER3", "COMPLETE"))
                )
            )
        )
    }

    @Test
    fun `get rosh full section returns correct response`() {
        val json = mockMvc
            .perform(get("/assessments/90123456/section/sectionroshfull").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<JsonNode>()

        assertThat(json["crn"].asText(), equalTo("T123456"))
        assertThat(json["nomsId"].asText(), equalTo("A1234YZ"))
        assertThat(json["dateCompleted"].asText(), equalTo("2024-01-15T11:30:00"))
        assertThat(json["currentConcernsRiskOfSelfHarm"].asText(), equalTo("Yes"))
    }

    @Test
    fun `get rosh summary section returns correct response`() {
        val json = mockMvc
            .perform(get("/assessments/90123456/section/sectionroshsumm").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<JsonNode>()

        assertThat(json["crn"].asText(), equalTo("T123456"))
        assertThat(json["nomsId"].asText(), equalTo("A1234YZ"))
        assertThat(json["dateCompleted"].asText(), equalTo("2024-01-15T11:30:00"))
        assertThat(json["riskPublicCommunity"].asText(), equalTo("High"))
    }

    @Test
    fun `get section returns 404 if oasys returns 404`() {
        mockMvc
            .perform(get("/assessments/90123451/section/sectionroshsumm").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get risk predictors returns correct response`() {
        val prediction = mockMvc
            .perform(
                get("/assessments/90123456/risk-predictors")
                    .queryParam("crn", "T123456").withToken()
            ).andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<RiskPrediction>()

        assertThat(
            prediction, equalTo(
                RiskPrediction(
                    YearPredictor(BigDecimal(45), BigDecimal(63), ScoreLevel.HIGH),
                    YearPredictor(BigDecimal(23), BigDecimal(36), ScoreLevel.MEDIUM),
                    YearPredictor(BigDecimal(29), BigDecimal(42), ScoreLevel.HIGH),
                    RsrPredictor(ScoreLevel.LOW, BigDecimal("3.45")),
                    SexualPredictor(
                        BigDecimal("0.11"),
                        BigDecimal("2"),
                        ScoreLevel.LOW,
                        ScoreLevel.HIGH,
                        null,
                        null,
                        null,
                        null
                    )
                )
            )
        )
    }
}

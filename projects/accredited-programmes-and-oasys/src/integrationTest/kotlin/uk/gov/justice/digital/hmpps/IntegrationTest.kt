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
import uk.gov.justice.digital.hmpps.integrations.oasys.Level
import uk.gov.justice.digital.hmpps.integrations.oasys.ScoredAnswer
import uk.gov.justice.digital.hmpps.integrations.oasys.ScoredAnswer.YesNo
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.math.BigDecimal
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.integrations.oasys.PniCalculation.Type as PniType

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
    fun `get timeline returns ok by CRN`() {
        mockMvc
            .perform(get("/assessments/timeline/T123456").withToken())
            .andExpect(status().isOk)
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

    @Test
    fun `get pni success`() {
        val res = mockMvc
            .perform(get("/assessments/pni/A8746PN?community=false").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PniResponse>()

        assertThat(
            res.pniCalculation,
            equalTo(
                PniCalculation(
                    LevelScore(Level.L, 0),
                    LevelScore(Level.M, 1),
                    LevelScore(Level.M, 1),
                    LevelScore(Level.H, 2),
                    Level.L,
                    Level.M,
                    4,
                    PniType.A,
                    SaraRiskLevel(1, 2),
                    listOf(),
                )
            )
        )

        assertThat(
            res.assessment,
            equalTo(
                PniAssessment(
                    id = 3875025120,
                    ldc = Ldc.from(0, 2),
                    ldcMessage = null,
                    ogrs3Risk = ScoreLevel.LOW,
                    ovpRisk = ScoreLevel.MEDIUM,

                    osp = Osp(
                        ScoreLevel.NOT_APPLICABLE,
                        ScoreLevel.NOT_APPLICABLE,
                    ),
                    0.56,
                    40,
                    questions = Questions(
                        YesNo.NO,
                        YesNo.NO,
                        ScoredAnswer.Problem.MISSING,
                        ScoredAnswer.Problem.MISSING,
                        ScoredAnswer.Problem.MISSING,
                        ScoredAnswer.Problem.SOME,
                        ScoredAnswer.Problem.MISSING,
                        ScoredAnswer.Problem.SOME,
                        ScoredAnswer.Problem.SIGNIFICANT,
                        ScoredAnswer.Problem.NONE,
                        ScoredAnswer.Problem.SOME,
                        ScoredAnswer.Problem.SIGNIFICANT,
                        ScoredAnswer.Problem.SOME,
                        ScoredAnswer.Problem.SIGNIFICANT,
                        ScoredAnswer.Problem.SIGNIFICANT,
                    )
                )
            )
        )
    }

    @Test
    fun `get pni for CRN`() {
        mockMvc
            .perform(get("/assessments/pni/P467261?community=false").withToken())
            .andExpect(status().isOk)
    }

    @Test
    fun `get pni no calculation`() {
        mockMvc
            .perform(get("/assessments/pni/A8747PN?community=true").withToken())
            .andExpect(status().isOk)
    }

    @Test
    fun `get pni returns 404 if oasys returns 404`() {
        mockMvc
            .perform(get("/assessments/pni/A1741PN?community=false").withToken())
            .andExpect(status().isNotFound)
    }
}

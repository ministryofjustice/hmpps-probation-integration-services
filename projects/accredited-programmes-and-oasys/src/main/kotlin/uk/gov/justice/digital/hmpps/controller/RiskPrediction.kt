package uk.gov.justice.digital.hmpps.controller

import uk.gov.justice.digital.hmpps.controller.ScoreLevel.Companion.of
import java.math.BigDecimal
import java.time.LocalDateTime

data class RiskPrediction(
    val completedDate: LocalDateTime? = null,
    val assessmentStatus: String? = null,
    val groupReconvictionScore: YearPredictor? = null,
    val violencePredictorScore: YearPredictor? = null,
    val generalPredictorScore: YearPredictor? = null,
    val riskOfSeriousRecidivismScore: RsrPredictor? = null,
    val sexualPredictorScore: SexualPredictor? = null,
)

data class YearPredictor(
    val oneYear: BigDecimal? = null,
    val twoYears: BigDecimal? = null,
    val scoreLevel: ScoreLevel? = null,
) {
    companion object {
        fun of(oneYear: BigDecimal?, twoYears: BigDecimal?, scoreLevel: String?): YearPredictor? =
            if (listOfNotNull(oneYear, twoYears, scoreLevel).isEmpty()) null else YearPredictor(
                oneYear,
                twoYears,
                of(scoreLevel)
            )
    }
}

data class RsrPredictor(
    val scoreLevel: ScoreLevel? = null,
    val percentageScore: BigDecimal? = null,
) {
    companion object {
        fun of(scoreLevel: String?, percentageScore: BigDecimal?): RsrPredictor? =
            if (scoreLevel == null && percentageScore == null) null else RsrPredictor(of(scoreLevel), percentageScore)
    }
}

data class SexualPredictor(
    val ospIndecentPercentageScore: BigDecimal?,
    val ospContactPercentageScore: BigDecimal?,
    val ospIndecentPercentageScoreLevel: ScoreLevel?,
    val ospContactPercentageScoreLevel: ScoreLevel?,
    val ospIndirectImagePercentageScore: BigDecimal?,
    val ospDirectContactPercentageScore: BigDecimal?,
    val ospIndirectImagePercentageScoreLevel: ScoreLevel?,
    val ospDirectContactPercentageScoreLevel: ScoreLevel?
) {
    companion object {
        fun of(
            ospIndecentPercentageScore: BigDecimal?,
            ospContactPercentageScore: BigDecimal?,
            ospIndecentPercentageScoreLevel: String?,
            ospContactPercentageScoreLevel: String?,
            ospIndirectImagePercentageScore: BigDecimal?,
            ospDirectContactPercentageScore: BigDecimal?,
            ospIndirectImagePercentageScoreLevel: String?,
            ospDirectContactPercentageScoreLevel: String?
        ): SexualPredictor? =
            if (listOfNotNull(
                    ospIndecentPercentageScore,
                    ospContactPercentageScore,
                    ospIndecentPercentageScoreLevel,
                    ospContactPercentageScoreLevel,
                    ospIndirectImagePercentageScore,
                    ospDirectContactPercentageScore,
                    ospIndirectImagePercentageScoreLevel,
                    ospDirectContactPercentageScoreLevel
                ).isEmpty()
            ) null
            else SexualPredictor(
                ospIndecentPercentageScore,
                ospContactPercentageScore,
                of(ospIndecentPercentageScoreLevel),
                of(ospContactPercentageScoreLevel),
                ospIndirectImagePercentageScore,
                ospDirectContactPercentageScore,
                of(ospIndirectImagePercentageScoreLevel),
                of(ospDirectContactPercentageScoreLevel)
            )
    }
}

enum class ScoreLevel(val type: String) {
    LOW("Low"), MEDIUM("Medium"), HIGH("High"), VERY_HIGH("Very High"), NOT_APPLICABLE("Not Applicable");

    companion object {
        fun of(type: String?): ScoreLevel? {
            return entries.firstOrNull { value -> value.type == type }
        }
    }
}
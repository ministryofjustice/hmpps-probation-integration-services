package uk.gov.justice.digital.hmpps.controller

import uk.gov.justice.digital.hmpps.integrations.oasys.ScoredAnswer

data class PniAssessment(
    val id: Long,
    val ldc: Ldc?,
    val ldcMessage: String?,
    val ogrs3Risk: ScoreLevel?,
    val ovpRisk: ScoreLevel?,
    val osp: Osp?,
    val rsrPercentage: Double?,
    val offenderAge: Int,
    val questions: Questions,
    val staticAllReoffendingPredictor: Predictor?,
    val dynamicAllReoffendingPredictor: Predictor?,
    val staticViolentReoffendingPredictor: Predictor?,
    val dynamicViolentReoffendingPredictor: Predictor?,
    val staticSeriousViolentReoffendingPredictor: Predictor?,
    val dynamicSeriousViolentReoffendingPredictor: Predictor?,

)

data class Ldc(val score: Int, val subTotal: Int) {
    companion object {
        fun from(score: Int?, subTotal: Int?): Ldc? =
            if (score == null || subTotal == null) null else Ldc(score, subTotal)
    }
}

data class Osp(val cdc: ScoreLevel?, val iiic: ScoreLevel?) {
    companion object {
        fun from(cdc: ScoreLevel?, iiic: ScoreLevel?): Osp? =
            if (cdc == null && iiic == null) null else Osp(cdc, iiic)
    }
}

data class Predictor(val score: Double?, val level: ScoreLevel?) {
    companion object {
        fun from(score: Double?, level: ScoreLevel?): Predictor? =
            if (score == null && level == null) null else Predictor(score, level)
    }
}
data class Questions(
    val everCommittedSexualOffence: ScoredAnswer.YesNo,
    val openSexualOffendingQuestions: ScoredAnswer.YesNo?,
    val sexualPreOccupation: ScoredAnswer.Problem,
    val offenceRelatedSexualInterests: ScoredAnswer.Problem,
    val emotionalCongruence: ScoredAnswer.Problem,
    val proCriminalAttitudes: ScoredAnswer.Problem,
    val hostileOrientation: ScoredAnswer.Problem,
    val relCloseFamily: ScoredAnswer.Problem,
    val prevCloseRelationships: ScoredAnswer.Problem,
    val easilyInfluenced: ScoredAnswer.Problem,
    val aggressiveControllingBehaviour: ScoredAnswer.Problem,
    val impulsivity: ScoredAnswer.Problem,
    val temperControl: ScoredAnswer.Problem,
    val problemSolvingSkills: ScoredAnswer.Problem,
    val difficultiesCoping: ScoredAnswer.Problem,
)
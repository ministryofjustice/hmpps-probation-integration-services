package uk.gov.justice.digital.hmpps.integrations.oasys

import uk.gov.justice.digital.hmpps.controller.*
import uk.gov.justice.digital.hmpps.integrations.oasys.ScoredAnswer.YesNo
import uk.gov.justice.digital.hmpps.controller.PniResponse as IntegrationModel

data class PniResult(val pniCalc: List<PniCalculation>?, val assessments: List<PniAssessment>?)
data class PniCalculation(
    val missingFields: List<String>?,
    val riskLevel: Level,
    val sexDomainLevel: Level,
    val sexDomainScore: Int,
    val thinkingDomainLevel: Level,
    val thinkingDomainScore: Int,
    val relationshipDomainLevel: Level,
    val relationshipDomainScore: Int,
    val selfManagementDomainLevel: Level,
    val selfManagementDomainScore: Int,
    val overallNeedLevel: Level,
    val totalDomainScore: Int,
    val pniCalculation: Type,
    val saraRiskLevelToPartner: Int,
    val saraRiskLevelToOther: Int,
) {
    enum class Type {
        H, M, A, O
    }
}

enum class Level {
    H, M, L
}

fun PniResult.asIntegrationModel(): IntegrationModel {
    val pniCalculation = with(pniCalc?.firstOrNull()) {
        if (this == null) null
        else uk.gov.justice.digital.hmpps.controller.PniCalculation(
            sexDomain = LevelScore(sexDomainLevel, sexDomainScore),
            thinkingDomain = LevelScore(thinkingDomainLevel, thinkingDomainScore),
            relationshipDomain = LevelScore(relationshipDomainLevel, relationshipDomainScore),
            selfManagementDomain = LevelScore(selfManagementDomainLevel, selfManagementDomainScore),
            riskLevel = riskLevel,
            needLevel = overallNeedLevel,
            totalDomainScore = totalDomainScore,
            pni = pniCalculation,
            saraRiskLevel = SaraRiskLevel(saraRiskLevelToPartner, saraRiskLevelToOther),
            missingFields = missingFields ?: listOf(),
        )
    }
    val assessment = assessments?.firstOrNull()?.let {
        uk.gov.justice.digital.hmpps.controller.PniAssessment(
            id = it.id,
            ldc = Ldc.from(it.ldcData.ldc, it.ldcData.ldcSubTotal),
            ldcMessage = it.ldcData.ldcMessage,
            ogrs3Risk = ScoreLevel.of(it.ogpOvp.ogrs3RiskRecon),
            ovpRisk = ScoreLevel.of(it.ogpOvp.ovpRisk),
            osp = Osp.from(
                ScoreLevel.of(it.rsrOspData.ospCdcScoreLevel),
                ScoreLevel.of(it.rsrOspData.ospIiicScoreLevel),
            ),
            it.rsrOspData.rsrPercentageScore,
            it.rsrOspData.offenderAge,
            questions = Questions(
                YesNo.of(it.everCommittedSexualOffence),
                YesNo.of(it.openSexualOffendingQuestions),
                ScoredAnswer.Problem.of(it.sexualPreOccupation),
                ScoredAnswer.Problem.of(it.offenceRelatedSexualInterests),
                ScoredAnswer.Problem.of(it.emotionalCongruence),
                ScoredAnswer.Problem.of(it.proCriminalAttitudes),
                ScoredAnswer.Problem.of(it.hostileOrientation),
                ScoredAnswer.Problem.of(it.relCloseFamily),
                ScoredAnswer.Problem.of(it.prevCloseRelationships),
                ScoredAnswer.Problem.of(it.easilyInfluenced),
                ScoredAnswer.Problem.of(it.aggressiveControllingBehaviour),
                ScoredAnswer.Problem.of(it.impulsivity),
                ScoredAnswer.Problem.of(it.temperControl),
                ScoredAnswer.Problem.of(it.problemSolvingSkills),
                ScoredAnswer.Problem.of(it.difficultiesCoping),
            )
        )
    }

    return IntegrationModel(
        pniCalculation = pniCalculation,
        assessment = assessment
    )
}
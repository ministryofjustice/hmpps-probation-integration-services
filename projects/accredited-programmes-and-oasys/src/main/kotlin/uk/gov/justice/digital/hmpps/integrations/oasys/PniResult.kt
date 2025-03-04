package uk.gov.justice.digital.hmpps.integrations.oasys

import uk.gov.justice.digital.hmpps.controller.LevelScore
import uk.gov.justice.digital.hmpps.controller.SaraRiskLevel
import uk.gov.justice.digital.hmpps.controller.PniCalculation as IntegrationModel

data class PniResult(val pniCalc: List<PniCalculation>?)
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

fun PniResult.asIntegrationModel(): IntegrationModel? = with(pniCalc?.firstOrNull()) {
    if (this == null) null
    else IntegrationModel(
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
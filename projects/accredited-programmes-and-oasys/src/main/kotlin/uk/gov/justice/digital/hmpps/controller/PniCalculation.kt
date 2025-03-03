package uk.gov.justice.digital.hmpps.controller

import uk.gov.justice.digital.hmpps.integrations.oasys.Level
import uk.gov.justice.digital.hmpps.integrations.oasys.PniCalculation.Type

data class PniCalculation(
    val sexDomain: LevelScore,
    val thinkingDomain: LevelScore,
    val relationshipDomain: LevelScore,
    val selfManagementDomain: LevelScore,
    val riskLevel: Level,
    val needLevel: Level,
    val totalDomainScore: Int,
    val pni: Type,
    val saraRiskLevel: SaraRiskLevel,
    val missingFields: List<String> = listOf(),
)

data class LevelScore(val level: Level, val score: Int)
data class SaraRiskLevel(val toPartner: Int, val toOther: Int)
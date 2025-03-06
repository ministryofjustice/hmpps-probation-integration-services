package uk.gov.justice.digital.hmpps.integrations.oasys

import com.fasterxml.jackson.annotation.JsonAlias

data class PniAssessment(
    @JsonAlias("assessmentPk")
    val id: Long,
    val everCommittedSexualOffence: String?,
    val openSexualOffendingQuestions: String?,
    val sexualPreOccupation: String?,
    val offenceRelatedSexualInterests: String?,
    val emotionalCongruence: String?,
    val proCriminalAttitudes: String?,
    val hostileOrientation: String?,
    val relCloseFamily: String?,
    val prevCloseRelationships: String?,
    val easilyInfluenced: String?,
    @JsonAlias("aggressiveControllingBehavour") // handle typo in actual response
    val aggressiveControllingBehaviour: String?,
    val impulsivity: String?,
    val temperControl: String?,
    val problemSolvingSkills: String?,
    val difficultiesCoping: String?,
    val ldcData: LdcData,
    val ogpOvp: OgpOvpData,
    val rsrOspData: RsrOspData,
)

data class LdcData(val ldc: Int?, val ldcSubTotal: Int?, val ldcMessage: String?)

data class OgpOvpData(val ogrs3RiskRecon: String?, val ovpRisk: String?)
data class RsrOspData(
    val ospCdcScoreLevel: String?,
    val ospIiicScoreLevel: String?,
    val rsrPercentageScore: Double?,
    val offenderAge: Int
)
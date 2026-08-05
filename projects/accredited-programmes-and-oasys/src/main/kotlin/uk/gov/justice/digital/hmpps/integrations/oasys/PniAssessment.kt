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
    val newActuarialPredictors: NewActuarialPredictorsData
)

data class LdcData(val ldc: Int?, val ldcSubTotal: Int?, val ldcMessage: String?)

data class OgpOvpData(val ogrs3RiskRecon: String?, val ovpRisk: String?)
data class RsrOspData(
    val ospCdcScoreLevel: String?,
    val ospIiicScoreLevel: String?,
    val rsrPercentageScore: Double?,
    val offenderAge: Int
)

data class NewActuarialPredictorsData(
    // All reoffending predictor - static
    val ogrs4gYr2: Double?,
    val ogrs4gBand: String?,
    val ogrs4gCalculated: String?,
    // All reoffending predictor - Dynamic
    val ogp2Yr2: Double?,
    val ogp2Band: String?,
    val ogp2Calculated: String?,
    // Violent reoffending Predictor - static
    val ogrs4vYr2: Double?,
    val ogrs4vBand: String?,
    val ogrs4vCalculated: String?,
    // Violent reoffending predictor - Dynamic
    val ovp2Yr2: Double?,
    val ovp2Band: String?,
    val ovp2Calculated: String?,
    // Serious Violent reoffending predictor - static
    val snsvStaticYr2: Double?,
    val snsvStaticYr2Band: String?,
    val snsvStaticCalculated: String?,
    // Serious Violent reoffending predictor - dynamic
    val snsvDynamicYr2: Double?,
    val snsvDynamicYr2Band: String?,
    val snsvDynamicCalculated: String?,
)

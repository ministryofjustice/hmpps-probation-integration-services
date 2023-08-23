package uk.gov.justice.digital.hmpps.integrations.oasys

import com.fasterxml.jackson.annotation.JsonAlias
import java.math.BigDecimal
import java.time.ZonedDateTime

data class OasysResponse<T : OasysAssessment>(
    val crn: String,
    @JsonAlias("limitedAccessOffender") val limitedAccess: Boolean,
    val assessments: List<T> = listOf()
)

interface OasysAssessment {
    val id: Long
    val type: String
    val status: String
    val superStatus: String
    val initiationDate: ZonedDateTime
    val dateCompleted: ZonedDateTime?
    val assessorSignedDate: ZonedDateTime?
    val laterWipAssessmentExists: Boolean
    val latestWipDate: ZonedDateTime?
    val laterSignLockAssessmentExists: Boolean
    val latestSignLockDate: ZonedDateTime?
    val laterPartCompUnsignedAssessmentExists: Boolean
    val latestPartCompUnsignedDate: ZonedDateTime?
    val laterPartCompSignedAssessmentExists: Boolean
    val latestPartCompSignedDate: ZonedDateTime?
    val laterCompleteAssessmentExists: Boolean
    val latestCompleteDate: ZonedDateTime?
}

data class RiskManagementPlan(
    @JsonAlias("assessmentPk") override val id: Long,
    @JsonAlias("assessmentType") override val type: String,
    @JsonAlias("assessmentStatus") override val status: String,
    override val superStatus: String,
    override val initiationDate: ZonedDateTime,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    @JsonAlias("laterWIPAssessmentExists") override val laterWipAssessmentExists: Boolean = false,
    @JsonAlias("latestWIPDate") override val latestWipDate: ZonedDateTime? = null,
    override val laterSignLockAssessmentExists: Boolean = false,
    override val latestSignLockDate: ZonedDateTime? = null,
    override val laterPartCompUnsignedAssessmentExists: Boolean = false,
    override val latestPartCompUnsignedDate: ZonedDateTime? = null,
    override val laterPartCompSignedAssessmentExists: Boolean = false,
    override val latestPartCompSignedDate: ZonedDateTime? = null,
    override val laterCompleteAssessmentExists: Boolean = false,
    override val latestCompleteDate: ZonedDateTime? = null,
    val keyInformationCurrentSituation: String? = null,
    val furtherConsiderationsCurrentSituation: String? = null,
    val supervision: String? = null,
    val monitoringAndControl: String? = null,
    val interventionsAndTreatment: String? = null,
    val victimSafetyPlanning: String? = null,
    val contingencyPlans: String? = null,
) : OasysAssessment

data class RiskPredictors(
    @JsonAlias("assessmentPk") override val id: Long,
    @JsonAlias("assessmentType") override val type: String,
    @JsonAlias("assessmentStatus") override val status: String,
    override val superStatus: String,
    override val initiationDate: ZonedDateTime,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    @JsonAlias("laterWIPAssessmentExists") override val laterWipAssessmentExists: Boolean = false,
    @JsonAlias("latestWIPDate") override val latestWipDate: ZonedDateTime? = null,
    override val laterSignLockAssessmentExists: Boolean = false,
    override val latestSignLockDate: ZonedDateTime? = null,
    override val laterPartCompUnsignedAssessmentExists: Boolean = false,
    override val latestPartCompUnsignedDate: ZonedDateTime? = null,
    override val laterPartCompSignedAssessmentExists: Boolean = false,
    override val latestPartCompSignedDate: ZonedDateTime? = null,
    override val laterCompleteAssessmentExists: Boolean = false,
    override val latestCompleteDate: ZonedDateTime? = null,
    @JsonAlias("OGP") val ogp: Ogp,
    @JsonAlias("OGRS") val ogrs: Ogrs,
    @JsonAlias("OSP") val osp: Osp,
    @JsonAlias("OVP") val ovp: Ovp,
    @JsonAlias("RSR") val rsr: Rsr,
) : OasysAssessment

data class OffenceDetails(
    @JsonAlias("assessmentPk") override val id: Long,
    @JsonAlias("assessmentType") override val type: String,
    @JsonAlias("assessmentStatus") override val status: String,
    override val superStatus: String,
    override val initiationDate: ZonedDateTime,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    @JsonAlias("laterWIPAssessmentExists") override val laterWipAssessmentExists: Boolean = false,
    @JsonAlias("latestWIPDate") override val latestWipDate: ZonedDateTime? = null,
    override val laterSignLockAssessmentExists: Boolean = false,
    override val latestSignLockDate: ZonedDateTime? = null,
    override val laterPartCompUnsignedAssessmentExists: Boolean = false,
    override val latestPartCompUnsignedDate: ZonedDateTime? = null,
    override val laterPartCompSignedAssessmentExists: Boolean = false,
    override val latestPartCompSignedDate: ZonedDateTime? = null,
    override val laterCompleteAssessmentExists: Boolean = false,
    override val latestCompleteDate: ZonedDateTime? = null,
    val offence: String? = null,
    val disinhibitors: List<String>? = listOf(),
    val patternOfOffending: String? = null,
    val offenceInvolved: List<String>? = listOf(),
    val specificWeapon: String? = null,
    val victimPerpetratorRelationship: String? = null,
    val victimOtherInfo: String? = null,
    val evidencedMotivations: List<String>? = listOf(),
    val offenceDetails: List<OffenceDetail>? = listOf(),
    val victimDetails: List<VictimDetail>? = listOf(),
) : OasysAssessment

data class OffenceDetail(
    val type: String,
    @JsonAlias("offenceDate") val date: ZonedDateTime,
    @JsonAlias("offenceCode") val code: String,
    @JsonAlias("offenceSubCode") val subCode: String,
    val offence: String,
    val subOffence: String
)

data class VictimDetail(
    val age: String? = null,
    val gender: String? = null,
    val ethnicCategory: String? = null,
    val victimRelation: String? = null,
)

data class Ogp(
    @JsonAlias("ogpStWesc") val stWesc: BigDecimal? = null,
    @JsonAlias("ogpDyWesc") val dyWesc: BigDecimal? = null,
    @JsonAlias("ogpTotWesc") val totWesc: BigDecimal? = null,
    @JsonAlias("ogp1Year") val oneYear: BigDecimal? = null,
    @JsonAlias("ogp2Year") val twoYear: BigDecimal? = null,
    @JsonAlias("ogpRisk") val risk: String? = null
)

data class Ogrs(
    @JsonAlias("ogrs31Year") val oneYear: BigDecimal? = null,
    @JsonAlias("ogrs32Year") val twoYear: BigDecimal? = null,
    @JsonAlias("ogrs3RiskRecon") val riskRecon: String? = null
)

data class Osp(
    @JsonAlias("ospImagePercentageScore") val imagePercentage: BigDecimal? = null,
    @JsonAlias("ospContactPercentageScore") val contactPercentage: BigDecimal? = null,
    @JsonAlias("ospImageScoreLevel") val imageLevel: String? = null,
    @JsonAlias("ospContactScoreLevel") val contactLevel: String? = null
)

data class Ovp(
    @JsonAlias("ovpStWesc") val stWesc: BigDecimal? = null,
    @JsonAlias("ovpDyWesc") val dyWesc: BigDecimal? = null,
    @JsonAlias("ovpTotWesc") val totWesc: BigDecimal? = null,
    @JsonAlias("ovp1Year") val oneYear: BigDecimal? = null,
    @JsonAlias("ovp2Year") val twoYear: BigDecimal? = null,
    @JsonAlias("ovpRisk") val risk: String? = null
)

data class Rsr(
    @JsonAlias("rsrPercentageScore") val score: BigDecimal? = null,
    @JsonAlias("rsrStaticOrDynamic") val type: String? = null,
    @JsonAlias("rsrAlgorithmVersion") val algorithmVersion: String? = null,
    @JsonAlias("scoreLevel") val level: String? = null
)

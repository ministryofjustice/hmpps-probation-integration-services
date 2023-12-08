package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRoshAssessment
import java.time.ZonedDateTime

data class RoshDetails(
    override val assessmentId: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    override val limitedAccessOffender: Boolean,
    override val lastUpdatedDate: ZonedDateTime? = null,
    val rosh: Rosh,
) : Assessment() {
    companion object {
        fun from(oasysRosh: OasysRoshAssessment): RoshDetails {
            with(oasysRosh.assessments[0]) {
                return RoshDetails(
                    assessmentPk,
                    assessmentType,
                    dateCompleted,
                    assessorSignedDate,
                    initiationDate,
                    assessmentStatus,
                    superStatus,
                    laterWIPAssessmentExists,
                    oasysRosh.limitedAccessOffender,
                    lastUpdatedDate,
                    Rosh(
                        RiskLevel.fromString(riskChildrenCommunity),
                        RiskLevel.fromString(riskPrisonersCustody),
                        RiskLevel.fromString(riskStaffCustody),
                        RiskLevel.fromString(riskStaffCommunity),
                        RiskLevel.fromString(riskKnownAdultCustody),
                        RiskLevel.fromString(riskKnownAdultCommunity),
                        RiskLevel.fromString(riskPublicCustody),
                        RiskLevel.fromString(riskPublicCommunity),
                        RiskLevel.fromString(riskChildrenCustody),
                    ),
                )
            }
        }
    }
}

data class Rosh(
    val riskChildrenCommunity: RiskLevel? = null,
    val riskPrisonersCustody: RiskLevel? = null,
    val riskStaffCustody: RiskLevel? = null,
    val riskStaffCommunity: RiskLevel? = null,
    val riskKnownAdultCustody: RiskLevel? = null,
    val riskKnownAdultCommunity: RiskLevel? = null,
    val riskPublicCustody: RiskLevel? = null,
    val riskPublicCommunity: RiskLevel? = null,
    val riskChildrenCustody: RiskLevel? = null,
)

enum class RiskLevel(val value: String) {
    VERY_HIGH("Very High"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    ;

    companion object {
        fun fromString(enumValue: String?): RiskLevel? {
            return values().firstOrNull { it.value == enumValue }
        }
    }
}

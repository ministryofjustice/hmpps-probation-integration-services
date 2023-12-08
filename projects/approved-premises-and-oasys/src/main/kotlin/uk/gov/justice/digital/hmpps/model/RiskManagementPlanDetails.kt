package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskManagementPlanDetails
import java.time.ZonedDateTime

data class RiskManagementPlanDetails(
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
    val riskManagementPlan: RiskManagementPlan,
) : Assessment() {
    companion object {
        fun from(oasysRiskManagementPlan: OasysRiskManagementPlanDetails): RiskManagementPlanDetails {
            with(oasysRiskManagementPlan.assessments[0]) {
                return RiskManagementPlanDetails(
                    assessmentPk,
                    assessmentType,
                    dateCompleted,
                    assessorSignedDate,
                    initiationDate,
                    assessmentStatus,
                    superStatus,
                    laterWIPAssessmentExists,
                    oasysRiskManagementPlan.limitedAccessOffender,
                    lastUpdatedDate,
                    RiskManagementPlan(
                        furtherConsiderations,
                        additionalComments,
                        contingencyPlans,
                        victimSafetyPlanning,
                        interventionsAndTreatment,
                        monitoringAndControl,
                        supervision,
                        keyInformationAboutCurrentSituation,
                    ),
                )
            }
        }
    }
}

data class RiskManagementPlan(
    val furtherConsiderations: String? = null,
    val additionalComments: String? = null,
    val contingencyPlans: String? = null,
    val victimSafetyPlanning: String? = null,
    val interventionsAndTreatment: String? = null,
    val monitoringAndControl: String? = null,
    val supervision: String? = null,
    val keyInformationAboutCurrentSituation: String? = null,
)

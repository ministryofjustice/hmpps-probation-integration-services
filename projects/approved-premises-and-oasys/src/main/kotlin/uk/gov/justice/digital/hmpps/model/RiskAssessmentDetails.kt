package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskAssessmentDetails
import java.time.ZonedDateTime

data class RiskAssessmentDetails(
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
    val riskAssessment: RiskAssessment,
) : Assessment() {
    companion object {
        fun from(oasysRiskAssessmentDetails: OasysRiskAssessmentDetails): RiskAssessmentDetails {
            with(oasysRiskAssessmentDetails.assessments[0]) {
                return RiskAssessmentDetails(
                    assessmentPk,
                    assessmentType,
                    dateCompleted,
                    assessorSignedDate,
                    initiationDate,
                    assessmentStatus,
                    superStatus,
                    laterWIPAssessmentExists,
                    oasysRiskAssessmentDetails.limitedAccessOffender,
                    lastUpdatedDate,
                    RiskAssessment(
                        currentOffenceDetails,
                        currentSources,
                        currentWhyDone,
                        currentAnyoneElsePresent,
                        currentWhoVictims,
                        currentHowDone,
                        currentWhereAndWhen,
                        previousSources,
                        previousWhyDone,
                        previousAnyoneElsePresent,
                        previousWhoVictims,
                        previousHowDone,
                        previousWhereAndWhen,
                        previousWhatDone,
                    ),
                )
            }
        }
    }
}

data class RiskAssessment(
    val currentOffenceDetails: String? = null,
    val currentSources: String? = null,
    val currentWhyDone: String? = null,
    val currentAnyoneElsePresent: String? = null,
    val currentWhoVictims: String? = null,
    val currentHowDone: String? = null,
    val currentWhereAndWhen: String? = null,
    val previousSources: String? = null,
    val previousWhyDone: String? = null,
    val previousAnyoneElsePresent: String? = null,
    val previousWhoVictims: String? = null,
    val previousHowDone: String? = null,
    val previousWhereAndWhen: String? = null,
    val previousWhatDone: String? = null,
)

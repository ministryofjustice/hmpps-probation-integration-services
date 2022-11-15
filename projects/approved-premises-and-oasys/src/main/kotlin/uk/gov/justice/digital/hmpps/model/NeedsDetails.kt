package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysNeedsDetails
import java.time.ZonedDateTime

data class NeedsDetails(
    override val assessmentId: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    override val limitedAccessOffender: Boolean,
    val needs: Needs? = null
) : Assessment() {
    companion object {
        fun from(oasysNeeds: OasysNeedsDetails): NeedsDetails {
            with(oasysNeeds.assessments[0]) {
                return NeedsDetails(
                    assessmentPk,
                    assessmentType,
                    dateCompleted,
                    assessorSignedDate,
                    initiationDate,
                    assessmentStatus,
                    superStatus,
                    laterWIPAssessmentExists,
                    oasysNeeds.limitedAccessOffender,
                    Needs(
                        offenceAnalysisDetails,
                        emoIssuesDetails,
                        drugIssuesDetails,
                        alcoholIssuesDetails,
                        lifestyleIssuesDetails,
                        relIssuesDetails,
                        financeIssuesDetails,
                        eTEIssuesDetails,
                        accIssuesDetails,
                        attIssuesDetails,
                        thingIssuesDetails,
                    )
                )
            }
        }
    }
}

data class Needs(
    val offenceAnalysisDetails: String? = null,
    val emotionalIssuesDetails: String? = null,
    val drugIssuesDetails: String? = null,
    val alcoholIssuesDetails: String? = null,
    val lifestyleIssuesDetails: String? = null,
    val relationshipIssuesDetails: String? = null,
    val financeIssuesDetails: String? = null,
    val educationTrainingEmploymentIssuesDetails: String? = null,
    val accommodationIssuesDetails: String? = null,
    val attitudeIssuesDetails: String? = null,
    val thinkingBehaviouralIssuesDetails: String? = null,
)

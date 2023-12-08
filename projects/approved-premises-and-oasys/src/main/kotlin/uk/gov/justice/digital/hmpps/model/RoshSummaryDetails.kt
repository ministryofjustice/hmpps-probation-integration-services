package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRoshSummary
import java.time.ZonedDateTime

data class RoshSummaryDetails(
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
    val roshSummary: RoshSummary,
) : Assessment() {
    companion object {
        fun from(oasysRoshSummary: OasysRoshSummary): RoshSummaryDetails {
            with(oasysRoshSummary.assessments[0]) {
                return RoshSummaryDetails(
                    assessmentPk,
                    assessmentType,
                    dateCompleted,
                    assessorSignedDate,
                    initiationDate,
                    assessmentStatus,
                    superStatus,
                    laterWIPAssessmentExists,
                    oasysRoshSummary.limitedAccessOffender,
                    lastUpdatedDate,
                    RoshSummary(
                        whoAtRisk,
                        riskReductionLikelyTo,
                        riskIncreaseLikelyTo,
                        riskGreatest,
                        natureOfRisk,
                    ),
                )
            }
        }
    }
}

data class RoshSummary(
    val whoIsAtRisk: String? = null,
    val riskReductionLikelyTo: String? = null,
    val riskIncreaseLikelyTo: String? = null,
    val riskGreatest: String? = null,
    val natureOfRisk: String? = null,
)

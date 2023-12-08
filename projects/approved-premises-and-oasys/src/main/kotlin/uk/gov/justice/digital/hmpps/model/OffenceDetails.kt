package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceDetails
import java.time.ZonedDateTime

data class OffenceDetails(
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
    val offence: Offence? = null,
) : Assessment() {
    companion object {
        fun from(oasysOffenceDetails: OasysOffenceDetails): OffenceDetails {
            with(oasysOffenceDetails.assessments[0]) {
                return OffenceDetails(
                    assessmentPk,
                    assessmentType,
                    dateCompleted,
                    assessorSignedDate,
                    initiationDate,
                    assessmentStatus,
                    superStatus,
                    laterWIPAssessmentExists,
                    oasysOffenceDetails.limitedAccessOffender,
                    lastUpdatedDate,
                    Offence(
                        offenceAnalysis,
                        othersInvolved,
                        issueContributingToRisk,
                        offenceMotivation,
                        victimImpact,
                        victimPerpetratorRel,
                        victimInfo,
                        patternOffending,
                        acceptsResponsibility,
                    ),
                )
            }
        }
    }
}

data class Offence(
    val offenceAnalysis: String? = null,
    val othersInvolved: String? = null,
    val issueContributingToRisk: String? = null,
    val offenceMotivation: String? = null,
    val victimImpact: String? = null,
    val victimPerpetratorRel: String? = null,
    val victimInfo: String? = null,
    val patternOffending: String? = null,
    val acceptsResponsibility: String? = null,
)

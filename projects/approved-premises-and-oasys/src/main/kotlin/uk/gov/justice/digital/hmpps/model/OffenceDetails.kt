package uk.gov.justice.digital.hmpps.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysOffenceDetails
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenceDetails(
    val assessmentId: Long,
    val assessmentType: String,
    val dateCompleted: ZonedDateTime? = null,
    val assessorSignedDate: ZonedDateTime? = null,
    val initiationDate: ZonedDateTime,
    val assessmentStatus: String,
    val superStatus: String? = null,
    val laterWIPAssessmentExists: Boolean? = null,
    val limitedAccessOffender: Boolean,
    val offence: Offence? = null
) {
    companion object {
        fun from(oasysOffenceDetails: OasysOffenceDetails) : OffenceDetails {
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
                    Offence(
                        offenceAnalysis,
                        othersInvolved,
                        issueContributingToRisk,
                        offenceMotivation,
                        victimImpact,
                        victimPerpetratorRel,
                        victimInfo,
                        patternOffending,
                        acceptsResponsibility
                    )
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
    val acceptsResponsibility: String? = null
)

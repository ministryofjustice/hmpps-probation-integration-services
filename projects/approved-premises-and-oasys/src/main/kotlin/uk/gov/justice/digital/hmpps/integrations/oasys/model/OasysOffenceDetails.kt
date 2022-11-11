package uk.gov.justice.digital.hmpps.integrations.oasys.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.ZonedDateTime

data class OasysOffenceDetails(
    val limitedAccessOffender: Boolean,
    val assessments: List<OasysOffenceAssessment>
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class OasysOffenceAssessment(
    val assessmentPk: Long,
    val assessmentType: String,
    val dateCompleted: ZonedDateTime? = null,
    val assessorSignedDate: ZonedDateTime? = null,
    val initiationDate: ZonedDateTime,
    val assessmentStatus: String,
    val superStatus: String? = null,
    val laterWIPAssessmentExists: Boolean? = null,
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

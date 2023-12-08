package uk.gov.justice.digital.hmpps.integrations.oasys.model

import java.time.ZonedDateTime

data class OasysOffenceDetails(
    val limitedAccessOffender: Boolean,
    val assessments: List<OasysOffenceAssessment>,
)

data class OasysOffenceAssessment(
    override val assessmentPk: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    override val lastUpdatedDate: ZonedDateTime? = null,
    val offenceAnalysis: String? = null,
    val othersInvolved: String? = null,
    val issueContributingToRisk: String? = null,
    val offenceMotivation: String? = null,
    val victimImpact: String? = null,
    val victimPerpetratorRel: String? = null,
    val victimInfo: String? = null,
    val patternOffending: String? = null,
    val acceptsResponsibility: String? = null,
) : OasysAssessment()

package uk.gov.justice.digital.hmpps.integrations.oasys.model

import java.time.ZonedDateTime

data class OasysRoshSummary(
    val limitedAccessOffender: Boolean,
    val assessments: List<OasysRoshSummaryAssessment>,
)

data class OasysRoshSummaryAssessment(
    override val assessmentPk: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    override val lastUpdatedDate: ZonedDateTime? = null,
    val whoAtRisk: String? = null,
    val riskReductionLikelyTo: String? = null,
    val riskIncreaseLikelyTo: String? = null,
    val riskGreatest: String? = null,
    val natureOfRisk: String? = null,
) : OasysAssessment()

package uk.gov.justice.digital.hmpps.integrations.oasys.model

import java.time.ZonedDateTime

data class OasysNeedsDetails(
    val limitedAccessOffender: Boolean,
    val assessments: List<OasysNeedsAssessment>
)

data class OasysNeedsAssessment(
    override val assessmentPk: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    val offenceAnalysisDetails: String? = null,
    val emoIssuesDetails: String? = null,
    val drugIssuesDetails: String? = null,
    val alcoholIssuesDetails: String? = null,
    val lifestyleIssuesDetails: String? = null,
    val relIssuesDetails: String? = null,
    val financeIssuesDetails: String? = null,
    val eTEIssuesDetails: String? = null,
    val accIssuesDetails: String? = null,
    val attIssuesDetails: String? = null,
    val thingIssuesDetails: String? = null,
) : OasysAssessment()

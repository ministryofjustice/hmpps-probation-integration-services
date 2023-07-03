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
    override val lastUpdatedDate: ZonedDateTime? = null,
    val offenceLinkedToHarm: String? = null,
    val offenceAnalysisDetails: String? = null,
    val emoLinkedToReoffending: String? = null,
    val emoLinkedToHarm: String? = null,
    val emoIssuesDetails: String? = null,
    val drugLinkedToReoffending: String? = null,
    val drugLinkedToHarm: String? = null,
    val drugIssuesDetails: String? = null,
    val alcoholLinkedToReoffending: String? = null,
    val alcoholLinkedToHarm: String? = null,
    val alcoholIssuesDetails: String? = null,
    val lifestyleLinkedToReoffending: String? = null,
    val lifestyleLinkedToHarm: String? = null,
    val lifestyleIssuesDetails: String? = null,
    val relLinkedToReoffending: String? = null,
    val relLinkedToHarm: String? = null,
    val relIssuesDetails: String? = null,
    val financeLinkedToReoffending: String? = null,
    val financeLinkedToHarm: String? = null,
    val financeIssuesDetails: String? = null,
    val eTELinkedToReoffending: String? = null,
    val eTELinkedToHarm: String? = null,
    val eTEIssuesDetails: String? = null,
    val accLinkedToReoffending: String? = null,
    val accLinkedToHarm: String? = null,
    val accIssuesDetails: String? = null,
    val attLinkedToReoffending: String? = null,
    val attLinkedToHarm: String? = null,
    val attIssuesDetails: String? = null,
    val thinkLinkedToReoffending: String? = null,
    val thinkLinkedToHarm: String? = null,
    val thingIssuesDetails: String? = null
) : OasysAssessment()

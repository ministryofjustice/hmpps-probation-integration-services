package uk.gov.justice.digital.hmpps.integrations.oasys.model

import java.time.ZonedDateTime

data class OasysRoshAssessment(
    val limitedAccessOffender: Boolean,
    val assessments: List<OasysRoshDetails>,
)

data class OasysRoshDetails(
    override val assessmentPk: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    override val lastUpdatedDate: ZonedDateTime? = null,
    val riskChildrenCommunity: String? = null,
    val riskPrisonersCustody: String? = null,
    val riskStaffCustody: String? = null,
    val riskStaffCommunity: String? = null,
    val riskKnownAdultCustody: String? = null,
    val riskKnownAdultCommunity: String? = null,
    val riskPublicCustody: String? = null,
    val riskPublicCommunity: String? = null,
    val riskChildrenCustody: String? = null,
) : OasysAssessment()

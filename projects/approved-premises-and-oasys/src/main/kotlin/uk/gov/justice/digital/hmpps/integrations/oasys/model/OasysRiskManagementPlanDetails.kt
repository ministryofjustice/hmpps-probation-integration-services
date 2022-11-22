package uk.gov.justice.digital.hmpps.integrations.oasys.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class OasysRiskManagementPlanDetails(
    val limitedAccessOffender: Boolean,
    val assessments: List<OasysRiskManagementPlanAssessment>
)

data class OasysRiskManagementPlanAssessment(

    override val assessmentPk: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    @JsonProperty("furtherConiderations")
    val furtherConsiderations: String? = null,
    val additionalComments: String? = null,
    val contingencyPlans: String? = null,
    val victimSafetyPlanning: String? = null,
    val interventionsAndTreatment: String? = null,
    val monitoringAndControl: String? = null,
    val supervision: String? = null,
    val keyInformationAboutCurrentSituation: String? = null

) : OasysAssessment()

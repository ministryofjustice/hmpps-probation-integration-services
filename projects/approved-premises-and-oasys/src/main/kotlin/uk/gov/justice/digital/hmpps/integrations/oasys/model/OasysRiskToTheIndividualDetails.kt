package uk.gov.justice.digital.hmpps.integrations.oasys.model

import java.time.ZonedDateTime

data class OasysRiskToTheIndividualDetails(
    val limitedAccessOffender: Boolean,
    val assessments: List<OasysRiskToTheIndividualAssessment>,
)

data class OasysRiskToTheIndividualAssessment(
    override val assessmentPk: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    override val lastUpdatedDate: ZonedDateTime? = null,
    val concernsRiskOfSuicide: String? = null,
    val currentConcernsBreachOfTrustText: String? = null,
    val currentConcernsBreachOfTrust: String? = null,
    val riskOfSeriousHarm: String? = null,
    val previousVulnerability: String? = null,
    val currentVulnerability: String? = null,
    val previousCustodyHostelCoping: String? = null,
    val currentCustodyHostelCoping: String? = null,
    val previousConcernsSelfHarmSuicide: String? = null,
    val currentConcernsSelfHarmSuicide: String? = null,
    val currentConcernsRiskOfSelfHarm: String? = null,
    val currentConcernsRiskOfSuicide: String? = null,
    val concernsBreachOfTrust: String? = null,
    val concernsRiskOfSelfHarm: String? = null,
) : OasysAssessment()

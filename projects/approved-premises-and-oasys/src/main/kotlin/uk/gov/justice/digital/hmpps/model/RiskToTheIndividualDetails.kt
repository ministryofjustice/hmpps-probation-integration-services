package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysRiskToTheIndividualDetails
import java.time.ZonedDateTime

data class RiskToTheIndividualDetails(
    override val assessmentId: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    override val limitedAccessOffender: Boolean,
    val riskToTheIndividual: RiskToTheIndividual
) : Assessment() {
    companion object {
        fun from(oasysRiskToTheIndividual: OasysRiskToTheIndividualDetails): RiskToTheIndividualDetails {
            with(oasysRiskToTheIndividual.assessments[0]) {
                return RiskToTheIndividualDetails(
                    assessmentPk,
                    assessmentType,
                    dateCompleted,
                    assessorSignedDate,
                    initiationDate,
                    assessmentStatus,
                    superStatus,
                    laterWIPAssessmentExists,
                    oasysRiskToTheIndividual.limitedAccessOffender,
                    RiskToTheIndividual(
                        concernsRiskOfSuicide,
                        currentConcernsBreachOfTrustText,
                        currentConcernsBreachOfTrust,
                        riskOfSeriousHarm,
                        previousVulnerability,
                        currentVulnerability,
                        previousCustodyHostelCoping,
                        currentCustodyHostelCoping,
                        previousConcernsSelfHarmSuicide,
                        currentConcernsSelfHarmSuicide,
                        currentConcernsRiskOfSelfHarm,
                        currentConcernsRiskOfSuicide,
                        concernsBreachOfTrust,
                        concernsRiskOfSelfHarm
                    )
                )
            }
        }
    }
}

data class RiskToTheIndividual(
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
)

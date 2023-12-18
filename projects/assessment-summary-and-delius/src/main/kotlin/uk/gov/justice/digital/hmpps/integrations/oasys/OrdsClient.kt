package uk.gov.justice.digital.hmpps.integrations.oasys

import org.springframework.web.service.annotation.GetExchange
import java.net.URI
import java.time.LocalDate

interface OrdsClient {
    @GetExchange
    fun getAssessmentSummary(uri: URI): AssessmentSummaries
}

data class AssessmentSummary(
    val assessmentPk: Long,
    val assessmentType: String,
    val dateCompleted: LocalDate,
    val initiationDate: LocalDate,
    val assessmentStatus: String,
    val initialSpDate: LocalDate? = null,
    val reviewSpDate: LocalDate? = null,
    val reviewNum: String? = null,
    val currentConcernsBreachOfTrust: String? = null,
    val currentConcernsCustody: String? = null,
    val currentConcernsDisruptive: String? = null,
    val currentConcernsEscape: String? = null,
    val currentConcernsHostel: String? = null,
    val currentConcernsRiskOfSelfHarm: String? = null,
    val currentConcernsRiskOfSuicide: String? = null,
    val currentConcernsVulnerablity: String? = null,
    val riskAdultCommunity: String? = null,
    val riskChildrenCommunity: String? = null,
    val riskChildrenCustody: String? = null,
    val riskKnownAdultCustody: String? = null,
    val riskPrisonersCustody: String? = null,
    val riskPublicCommunity: String? = null,
    val riskPublicCustody: String? = null,
    val riskStaffCommunity: String? = null,
    val riskStaffCustody: String? = null,
    val weightedScores: WeightedScores = WeightedScores(),
    val furtherInformation: FurtherInformation = FurtherInformation(),
    val ogpOvp: OgpOvp = OgpOvp(),
    val offences: List<Offence> = arrayListOf(),
    val basicSentencePlan: String? = null,
    val sentencePlan: SentencePlan? = null
) {
    val riskFlags: List<String> = listOf(
        riskAdultCommunity,
        riskChildrenCommunity,
        riskChildrenCustody,
        riskKnownAdultCustody,
        riskPrisonersCustody,
        riskPublicCommunity,
        riskPublicCustody,
        riskStaffCommunity,
        riskStaffCustody
    ).map { it?.firstOrNull()?.uppercase() ?: "N" }

    val concernFlags: List<String> = listOf(
        currentConcernsBreachOfTrust,
        currentConcernsCustody,
        currentConcernsDisruptive,
        currentConcernsEscape,
        currentConcernsHostel,
        currentConcernsRiskOfSelfHarm,
        currentConcernsRiskOfSuicide,
        currentConcernsVulnerablity
    ).map { it?.uppercase() ?: "DK" }
}

data class WeightedScores(
    val accommodationWeightedScore: Long? = null,
    val eteWeightedScore: Long? = null,
    val relationshipsWeightedScore: Long? = null,
    val lifestyleWeightedScore: Long? = null,
    val drugWeightedScore: Long? = null,
    val alcoholWeightedScore: Long? = null,
    val thinkingWeightedScore: Long? = null,
    val attitudesWeightedScore: Long? = null
)

data class FurtherInformation(
    val totWeightedScore: Long? = null,
    val pOAssessment: String? = null,
    val pOAssessmentDesc: String? = null,
    val assessorName: String? = null,
    val ogrs1Year: Long? = null,
    val ogrs2Year: Long? = null,
    val reviewTerm: String? = null,
    val cmsEventNumber: Long? = null,
    val courtCode: String? = null,
    val courtType: String? = null,
    val courtName: String? = null
)

data class OgpOvp(
    val ogp1Year: Long? = null,
    val ogp2Year: Long? = null,
    val ovp1Year: Long? = null,
    val ovp2Year: Long? = null,
)

data class Offence(
    val offenceCode: String? = null,
    val offenceSubcode: String? = null
)

data class SentencePlan(
    val objectives: List<Objective> = listOf()
)

data class Objective(
    val objectiveCode: String,
    val objectiveMeasure: String,
    val objectiveSequence: Long
)

data class AssessmentSummaries(
    val crn: String,
    val assessments: List<AssessmentSummary>
)

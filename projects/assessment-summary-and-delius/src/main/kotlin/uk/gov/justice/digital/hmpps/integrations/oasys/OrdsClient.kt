package uk.gov.justice.digital.hmpps.integrations.oasys

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
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
    @JsonFormat(pattern = "[yyyy-MM-dd][dd/MM/yyyy]") // temporary until oasys bug fixed, should not be used for responses
    val initialSpDate: LocalDate? = null,
    @JsonFormat(pattern = "[yyyy-MM-dd][dd/MM/yyyy]") // temporary until oasys bug fixed, should not be used for responses
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
    val riskKnownAdultCommunity: String? = null,
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
    val basicSentencePlan: List<BasicSentencePlanArea>? = null,
    val sentencePlan: SentencePlan? = null
) {
    /*
        Order of Risk has to match Delius read order:
        Children, Public, KnownAdult, Staff, Prisoner - community variant of each first
    */
    val riskFlags: List<String> = listOf(
        riskChildrenCommunity,
        riskChildrenCustody,
        riskPublicCommunity,
        riskPublicCustody,
        riskKnownAdultCommunity,
        riskKnownAdultCustody,
        riskStaffCommunity,
        riskStaffCustody,
        riskPrisonersCustody
    ).map { it?.firstOrNull()?.uppercase() ?: "N" }

    /*
        Order of Concerns has to match Delius read order:
        Suicide, Harm, Custody, Hostel, Vulnerability, Abscond, Disruptive Behaviour, Breach of Trust
    */
    val concernFlags: List<String> = listOf(
        currentConcernsRiskOfSuicide,
        currentConcernsRiskOfSelfHarm,
        currentConcernsCustody,
        currentConcernsHostel,
        currentConcernsVulnerablity,
        currentConcernsEscape,
        currentConcernsDisruptive,
        currentConcernsBreachOfTrust
    ).map {
        when (it?.first()?.uppercase()) {
            "Y" -> "YES"
            "N" -> "NO"
            else -> "DK"
        }
    }
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
    val objectiveCodeDesc: String,
    val objectiveSequence: Long,
    val criminogenicNeeds: List<Need>,
    val actions: List<Action>?
)

data class Need(
    val criminogenicNeedDesc: String
)

data class Action(
    val actionDesc: String,
    val actionComment: String?
)

data class AssessmentSummaries(
    @JsonAlias("probNumber")
    val crn: String,
    val assessments: List<AssessmentSummary>
)

data class BasicSentencePlanArea(
    val bspAreaLinked: String?,
    val bspAreaLinkedDesc: String?
)

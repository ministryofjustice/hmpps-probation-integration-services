package uk.gov.justice.digital.hmpps.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.oasys.client.OasysClient
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysTimelineAssessment
import uk.gov.justice.digital.hmpps.model.HealthDetails
import uk.gov.justice.digital.hmpps.model.NeedsDetails
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.model.RiskAssessmentDetails
import uk.gov.justice.digital.hmpps.model.RiskManagementPlanDetails
import uk.gov.justice.digital.hmpps.model.RiskToTheIndividualDetails
import uk.gov.justice.digital.hmpps.model.RoshDetails
import uk.gov.justice.digital.hmpps.model.RoshSummaryDetails

@Service
class OasysAssessmentService(private var oasysClient: OasysClient) {
    fun getLatestAssessment(crn: String): OasysTimelineAssessment {
        try {
            val ordsAssessmentTimeline = oasysClient.getAssessmentTimeline(crn)
            val assessments =
                ordsAssessmentTimeline.timeline.sortedByDescending { it.initiationDate }.stream().filter {
                    it.assessmentType == "LAYER3"
                }
            return assessments.findFirst().orElseThrow {
                NotFoundException("Latest layer 3 assessment not found for crn=$crn")
            }
        } catch (ex: HttpStatusCodeException) {
            when (ex.statusCode) {
                HttpStatus.NOT_FOUND -> throw NotFoundException(ex.localizedMessage)
                else -> throw ex
            }
        }
    }

    fun getOffenceDetails(crn: String): OffenceDetails {
        val latestAssessment = getLatestAssessment(crn)
        return OffenceDetails.from(
            oasysClient.getOffenceDetails(
                crn,
                latestAssessment.assessmentPk,
                latestAssessment.status,
            ),
        )
    }

    fun getNeedsDetails(crn: String): NeedsDetails {
        val latestAssessment = getLatestAssessment(crn)
        return NeedsDetails.from(
            oasysClient.getNeedsDetails(
                crn,
                latestAssessment.assessmentPk,
                latestAssessment.status,
            ),
        )
    }

    fun getRiskManagementPlanDetails(crn: String): RiskManagementPlanDetails {
        val latestAssessment = getLatestAssessment(crn)
        return RiskManagementPlanDetails.from(
            oasysClient.getRiskManagementPlanDetails(
                crn,
                latestAssessment.assessmentPk,
                latestAssessment.status,
            ),
        )
    }

    fun getRoshSummary(crn: String): RoshSummaryDetails {
        val latestAssessment = getLatestAssessment(crn)
        return RoshSummaryDetails.from(
            oasysClient.getRoshSummary(
                crn,
                latestAssessment.assessmentPk,
                latestAssessment.status,
            ),
        )
    }

    fun getRiskToIndividual(crn: String): RiskToTheIndividualDetails {
        val latestAssessment = getLatestAssessment(crn)
        return RiskToTheIndividualDetails.from(
            oasysClient.getRiskToTheIndividual(
                crn,
                latestAssessment.assessmentPk,
                latestAssessment.status,
            ),
        )
    }

    fun getRiskAssessment(crn: String): RiskAssessmentDetails {
        val latestAssessment = getLatestAssessment(crn)
        return RiskAssessmentDetails.from(
            oasysClient.getRiskAssessment(
                crn,
                latestAssessment.assessmentPk,
                latestAssessment.status,
            ),
        )
    }

    fun getRosh(crn: String): RoshDetails {
        val latestAssessment = getLatestAssessment(crn)
        return RoshDetails.from(
            oasysClient.getRiskOfSeriousHarm(
                crn,
                latestAssessment.assessmentPk,
                latestAssessment.status,
            ),
        )
    }

    fun getHealthDetails(crn: String): HealthDetails {
        val latestAssessment = getLatestAssessment(crn)
        return HealthDetails.from(
            oasysClient.getHealthDetails(
                crn,
                latestAssessment.assessmentPk,
                latestAssessment.status,
            ),
        )
    }
}

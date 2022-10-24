package uk.gov.justice.digital.hmpps.controller

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.oasys.OasysAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.OasysClient

@Service
class OasysAssessmentService(private var oasysClient: OasysClient) {
    fun getLatestAssessment(crn: String): OasysAssessment {
        val ordsAssessmentTimeline = oasysClient.getAssessmentTimeline(crn) ?: throw NotFoundException("No Assessments were found for crn= $crn")
        val assessments =
            ordsAssessmentTimeline.timeline.sortedByDescending { it.completedDate }.stream().filter {
                it.assessmentType == "LAYER3" &&
                    it.status == "COMPLETE"
            }
        return assessments.findFirst().orElseThrow{
            NotFoundException("Latest layer 3 assessment not found for crn=$crn")
        }
    }
}

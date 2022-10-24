package uk.gov.justice.digital.hmpps.controller

import feign.FeignException.NotFound
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.oasys.OasysAssessment
import uk.gov.justice.digital.hmpps.integrations.oasys.OasysClient

@Service
class OasysAssessmentService(private var oasysClient: OasysClient) {
    fun getLatestAssessment(crn: String): OasysAssessment {
        return try {
            val ordsAssessmentTimeline = oasysClient.getAssessmentTimeline(crn)
            val assessment =
                ordsAssessmentTimeline?.timeline?.sortedByDescending { it.completedDate }?.stream()?.filter {
                    it.assessmentType == "LAYER3" &&
                        it.status == "COMPLETE"
                }?.findFirst() ?: throw NotFoundException("Latest layer 3 assessment not found")
            assessment.get()
        } catch (e: NotFound) {
            throw NotFoundException("Latest layer 3 assessment not found")
        }
    }
}

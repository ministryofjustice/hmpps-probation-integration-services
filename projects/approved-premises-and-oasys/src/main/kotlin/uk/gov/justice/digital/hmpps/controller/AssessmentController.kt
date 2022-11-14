package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysTimelineAssessment
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.service.OasysAssessmentService

@RestController
class AssessmentController(private var oasysAssessmentService: OasysAssessmentService) {
    @PreAuthorize("hasRole('ROLE_APPROVED_PREMISES_ASSESSMENTS')")
    @GetMapping(value = ["/latest-assessment/{crn}"])
    fun getAssessmentTimeline(
        @PathVariable("crn") crn: String
    ): OasysTimelineAssessment {
        return oasysAssessmentService.getLatestAssessment(crn)
    }

    @PreAuthorize("hasRole('ROLE_APPROVED_PREMISES_ASSESSMENTS')")
    @GetMapping(value = ["/offence-details/{crn}"])
    fun getOffenceDetails(
        @PathVariable("crn") crn: String
    ): OffenceDetails {
        return oasysAssessmentService.getOffenceDetails(crn)
    }
}

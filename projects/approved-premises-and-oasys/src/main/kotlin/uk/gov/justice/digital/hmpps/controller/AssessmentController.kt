package uk.gov.justice.digital.hmpps.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.oasys.OasysAssessment

@RestController
class AssessmentController(private var oasysAssessmentService: OasysAssessmentService) {
    // @PreAuthorize("hasRole('ROLE_EXAMPLE')")
    @GetMapping(value = ["/latest-assessment/{crn}"])
    fun handle(
        @PathVariable("crn") crn: String
    ): OasysAssessment {
        return oasysAssessmentService.getLatestAssessment(crn)
    }
}

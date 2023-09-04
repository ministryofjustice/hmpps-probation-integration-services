package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.AssessmentService

@RestController
@RequestMapping("assessments")
class AssessmentResource(private val assessmentService: AssessmentService) {
    @PreAuthorize("hasRole('ARNS_ASSESSMENTS')")
    @GetMapping("offences/{crn}/{lao}")
    fun getOffenceDetails(@PathVariable("crn") crn: String, @PathVariable("lao") lao: String) =
        assessmentService.getOffenceDetails(crn, lao)

    @PreAuthorize("hasRole('ARNS_ASSESSMENTS')")
    @GetMapping("risk-management-plans/{crn}/{lao}")
    fun getRiskManagementPlans(@PathVariable("crn") crn: String, @PathVariable("lao") lao: String) =
        assessmentService.getRiskManagementPlans(crn, lao)

    @PreAuthorize("hasRole('ARNS_ASSESSMENTS')")
    @GetMapping("all-risk-predictors/{crn}/{lao}")
    fun getRiskPredictors(@PathVariable("crn") crn: String, @PathVariable("lao") lao: String) =
        assessmentService.getRiskPredictors(crn, lao)

    @PreAuthorize("hasRole('ARNS_ASSESSMENTS')")
    @GetMapping("timeline/{crn}/{lao}")
    fun getTimeline(@PathVariable("crn") crn: String, @PathVariable("lao") lao: String) =
        assessmentService.getTimeline(crn, lao)
}

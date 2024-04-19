package uk.gov.justice.digital.hmpps.api

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.ProbationCaseService

@RestController
@RequestMapping("probation-cases/{crn}")
class ProbationCaseResource(private val probationCaseService: ProbationCaseService) {
    @PreAuthorize("hasRole('PROBATION_API__OASYS__CASE_DETAIL')")
    @GetMapping
    fun getCaseDetails(@PathVariable("crn") crn: String) =
        probationCaseService.findCase(crn)
}
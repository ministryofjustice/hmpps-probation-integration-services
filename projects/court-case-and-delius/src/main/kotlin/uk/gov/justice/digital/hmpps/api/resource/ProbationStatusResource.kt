package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.ProbationCase
import uk.gov.justice.digital.hmpps.service.ProbationStatusService

@RestController
@RequestMapping("probation-case/{crn}")
class ProbationStatusResource(private val probationStatusService: ProbationStatusService) {
    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping("/status")
    fun getProbationStatus(@PathVariable crn: String) = probationStatusService.getProbationStatus(crn)

    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping("/search")
    fun getProbationCase(@PathVariable crn: String): ProbationCase = probationStatusService.getProbationCase(crn)
}

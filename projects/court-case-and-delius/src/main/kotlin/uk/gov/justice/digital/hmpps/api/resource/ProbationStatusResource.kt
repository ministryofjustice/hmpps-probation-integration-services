package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ProbationStatusService

@RestController
@RequestMapping("probation-case/{crn}/status")
class ProbationStatusResource(private val probationStatusService: ProbationStatusService) {
    @PreAuthorize("hasAnyRole('COURT_CASE_PROBATION_STATUS','PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping
    fun getProbationStatus(@PathVariable crn: String) = probationStatusService.getProbationStatus(crn)
}

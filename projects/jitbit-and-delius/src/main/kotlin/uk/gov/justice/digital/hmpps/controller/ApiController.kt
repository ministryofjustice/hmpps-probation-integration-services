package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseDetailService

@RestController
class ApiController(
    private val caseDetailService: CaseDetailService
) {
    @GetMapping(value = ["/case/{crn}"])
    @PreAuthorize("hasRole('PROBATION_API__JITBIT__CASE_DETAIL')")
    fun getCaseDetails(@PathVariable crn: String) = caseDetailService.getCaseDetails(crn)
}

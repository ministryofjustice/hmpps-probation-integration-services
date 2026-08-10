package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseDetailService

@RestController
@RequestMapping("/arns")
@PreAuthorize("hasRole('PROBATION_API__ARNS__CASE_DETAIL')")
class CaseController(
    private val caseDetailService: CaseDetailService) {
    @GetMapping("/{crn}")
    fun getCaseDetail(@PathVariable crn: String) = caseDetailService.getCaseDetail(crn)
}

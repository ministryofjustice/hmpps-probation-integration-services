package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseDetailService

@RestController
@PreAuthorize("hasRole('PROBATION_API__ARNS__CASE_DETAIL')")
class CaseController(
    private val caseDetailService: CaseDetailService
) {
    @GetMapping("/arns/{crn}")
    fun getCaseDetail(@PathVariable crn: String) = caseDetailService.getCaseDetail(crn)

    @GetMapping("/case/{crn}/additional-sentences")
    @Operation(summary = "Get the additional sentences for the case.")
    fun getAdditionalSentences(@PathVariable crn: String) = caseDetailService.getAdditionalSentences(crn)
}

package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.CaseDetail
import uk.gov.justice.digital.hmpps.model.CaseSummaries
import uk.gov.justice.digital.hmpps.service.CaseService

@RestController
@RequestMapping("probation-cases")
@PreAuthorize("hasRole('PROBATION_API__APPROVED_PREMISES__CASE_DETAIL')")
class CaseController(private val caseService: CaseService) {
    @RequestMapping(value = ["/summaries"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getCaseSummaries(
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 CRNs or NOMIS ids")
        @RequestBody ids: List<String>
    ): CaseSummaries = caseService.getCaseSummaries(ids)

    @GetMapping("/{id}/details")
    fun getCaseDetail(@PathVariable id: String): CaseDetail = caseService.getCaseDetail(id)
}

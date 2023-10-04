package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.CaseDetail
import uk.gov.justice.digital.hmpps.model.CaseSummaries
import uk.gov.justice.digital.hmpps.service.CaseService

@RestController
@RequestMapping("probation-cases")
class CaseController(private val caseService: CaseService) {

    @PreAuthorize("hasRole('ROLE_APPROVED_PREMISES_STAFF')")
    @RequestMapping(value = ["/summaries"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getCaseSummaries(
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") @RequestBody crns: List<String>
    ): CaseSummaries = caseService.getCaseSummaries(crns)

    @PreAuthorize("hasRole('ROLE_APPROVED_PREMISES_STAFF')")
    @GetMapping("/{crn}/detail")
    fun getCaseDetail(@PathVariable crn: String): CaseDetail = TODO()
}

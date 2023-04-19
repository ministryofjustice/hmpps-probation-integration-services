package uk.gov.justice.digital.hmpps.epf

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController(private val caseDetailsService: CaseDetailsService) {
    @PreAuthorize("hasRole('EPF_CONTEXT')")
    @GetMapping(value = ["/case-details/{crn}/{eventNumber}"])
    fun handle(
        @PathVariable("crn") crn: String,
        @PathVariable("eventNumber") eventNumber: Int
    ) = caseDetailsService.caseDetails(crn, eventNumber)
}

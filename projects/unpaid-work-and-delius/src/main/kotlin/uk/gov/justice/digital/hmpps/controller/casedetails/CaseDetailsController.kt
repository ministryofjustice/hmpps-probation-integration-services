package uk.gov.justice.digital.hmpps.controller.casedetails

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CaseDetailsController(val service: CaseDetailsService) {
    @GetMapping(value = ["/case-data/{crn}/{eventId}"])
    @PreAuthorize("hasRole('UPW_DETAILS')")
    fun personDetails(
        @PathVariable crn: String,
        @PathVariable eventId: Long,
    ) = service.getCaseDetails(crn, eventId)
}

package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseViewService

@RestController
@RequestMapping("/allocation-demand")
class CaseViewResource(val service: CaseViewService) {
    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("/{crn}/{eventNumber}/case-view")
    fun caseView(@PathVariable crn: String, @PathVariable eventNumber: String) = service.caseView(crn, eventNumber)
}

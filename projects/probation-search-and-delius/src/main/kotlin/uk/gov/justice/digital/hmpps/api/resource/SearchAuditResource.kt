package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.ContactSearchAuditRequest
import uk.gov.justice.digital.hmpps.service.SearchAuditService

@RestController
@RequestMapping("probation-search/audit")
class SearchAuditResource(private val searchAuditService: SearchAuditService) {
    @PreAuthorize("hasRole('PROBATION_API__PROBATION_SEARCH__AUDIT_RW')")
    @PostMapping("/contact-search")
    @ResponseStatus(HttpStatus.CREATED)
    fun auditContactSearch(@RequestBody auditRequest: ContactSearchAuditRequest) {
        searchAuditService.auditContactSearch(auditRequest)
    }
}
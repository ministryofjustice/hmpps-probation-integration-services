package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseListService

@RestController
class SingleAccommodationController(
    private val caseListService: CaseListService
) {
    @PreAuthorize("hasRole('PROBATION_API__SINGLE_ACCOMMODATION__CASE_LIST')")
    @GetMapping(value = ["/case-list/{username}"])
    fun getCaseList(@PathVariable username: String) = caseListService.getCaseList(username)
}

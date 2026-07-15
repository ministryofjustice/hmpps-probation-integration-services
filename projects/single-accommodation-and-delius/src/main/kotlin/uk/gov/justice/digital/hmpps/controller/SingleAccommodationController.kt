package uk.gov.justice.digital.hmpps.controller

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseListService

@RestController
class SingleAccommodationController(
    private val caseListService: CaseListService,
) {
    @PreAuthorize("hasRole('PROBATION_API__SINGLE_ACCOMMODATION__CASE_LIST')")
    @GetMapping(value = ["/case-list/{username}"])
    fun getCaseList(
        @PathVariable username: String,
        @RequestParam(required = false) teamCode: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ) = caseListService.getCaseList(username, teamCode, PageRequest.of(page, size, Sort.by("crn")))

    @PreAuthorize("hasRole('PROBATION_API__SINGLE_ACCOMMODATION__CASE_LIST')")
    @GetMapping(value = ["/case/{username}/{crn}"])
    fun getCase(@PathVariable crn: String, @PathVariable username: String) = caseListService.getCase(username, crn)
}

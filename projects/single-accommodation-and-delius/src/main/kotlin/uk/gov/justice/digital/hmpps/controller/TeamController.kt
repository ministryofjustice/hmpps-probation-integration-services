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
class TeamController(
    private val caseListService: CaseListService,
) {
    @PreAuthorize("hasRole('PROBATION_API__SINGLE_ACCOMMODATION__CASE_LIST')")
    @GetMapping(value = ["/team/{teamCode}/case-list"])
    fun getCaseList(
        @PathVariable teamCode: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ) = caseListService.getTeamCaseIds(teamCode, PageRequest.of(page, size, Sort.by("crn")))
}

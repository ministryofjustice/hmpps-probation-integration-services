package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.SearchRequest
import uk.gov.justice.digital.hmpps.service.ProbationCaseSearch

@RestController
@RequestMapping(value = ["/search"])
class SearchController(private val search: ProbationCaseSearch) {
    @PreAuthorize("hasRole('PROBATION_API__HMPPS_API__CASE_DETAIL')")
    @PostMapping(value = ["/probation-cases"])
    fun searchProbationCases(
        @RequestParam(required = false, defaultValue = "true") useSearch: Boolean,
        @Valid @RequestBody request: SearchRequest
    ) = search.find(request, useSearch)
}
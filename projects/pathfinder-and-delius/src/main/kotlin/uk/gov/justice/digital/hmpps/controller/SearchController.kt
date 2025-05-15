package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.OffenderDetail
import uk.gov.justice.digital.hmpps.model.SearchRequest
import uk.gov.justice.digital.hmpps.service.ProbationCaseSearch
import java.util.*

@RestController
@RequestMapping(value = ["/search"])
class SearchController(private val search: ProbationCaseSearch) {
    @PreAuthorize("hasRole('PROBATION_API__PATHFINDER__CASE_DETAIL')")
    @PostMapping(value = ["/probation-cases"])
    fun searchProbationCases(
        @Valid @RequestBody request: SearchRequest,
        @RequestParam(required = false, defaultValue = "true") useSearch: Boolean
    ): List<OffenderDetail> = search.find(request, useSearch)

    @PreAuthorize("hasRole('PROBATION_API__PATHFINDER__CASE_DETAIL')")
    @PostMapping(value = ["/probation-cases/crns"])
    fun searchProbationCases(@NotEmpty @RequestBody crns: SequencedSet<String>): List<OffenderDetail> =
        search.crns(crns)
}
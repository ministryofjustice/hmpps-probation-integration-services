package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.ProbationCases
import uk.gov.justice.digital.hmpps.model.SearchRequest
import uk.gov.justice.digital.hmpps.service.ProbationCaseSearch
import java.util.SortedSet

@RestController
@RequestMapping(value = ["/search"])
class SearchController(private val search: ProbationCaseSearch) {
    @PreAuthorize("hasRole('PROBATION_API__SOC__CASE_DETAIL')")
    @PostMapping(value = ["/probation-cases"])
    fun searchProbationCases(@Valid @RequestBody request: SearchRequest): ProbationCases = search.find(request)

    @PreAuthorize("hasRole('PROBATION_API__SOC__CASE_DETAIL')")
    @PostMapping(value = ["/probation-cases/crns"])
    fun searchProbationCases(@NotEmpty @RequestBody crns: SortedSet<String>): ProbationCases = search.crns(crns)
}
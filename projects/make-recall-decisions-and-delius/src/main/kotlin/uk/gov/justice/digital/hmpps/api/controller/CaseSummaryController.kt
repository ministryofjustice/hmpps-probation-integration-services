package uk.gov.justice.digital.hmpps.api.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseSummaryService

@RestController
@RequestMapping("/case-summary/{crn}")
@PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISIONS_API')")
class CaseSummaryController(private val caseSummaryService: CaseSummaryService) {

    @GetMapping(value = ["/personal-details"])
    fun getPersonalDetails(@PathVariable("crn") crn: String) = caseSummaryService.getPersonalDetails(crn)
}

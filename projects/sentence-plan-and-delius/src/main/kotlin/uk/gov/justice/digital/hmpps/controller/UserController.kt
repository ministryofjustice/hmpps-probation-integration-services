package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.UserCaseloadIndicator
import uk.gov.justice.digital.hmpps.service.CaseloadService

@RestController
@RequestMapping(path = ["/users/{username}"])
class UserController(private val caseloadService: CaseloadService) {
    @PreAuthorize("hasRole('PROBATION_API__SENTENCE_PLAN__CASE_DETAIL')")
    @GetMapping(value = ["/access/{crn}"])
    fun getCaseDetails(@PathVariable username: String, @PathVariable crn: String): UserCaseloadIndicator =
        caseloadService.isInCaseload(username, crn)
}
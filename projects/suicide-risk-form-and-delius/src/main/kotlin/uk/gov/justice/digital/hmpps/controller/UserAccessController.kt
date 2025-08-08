package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
class UserAccessController(private val userAccessService: UserAccessService) {
    @PreAuthorize("hasRole('PROBATION_API__SUICIDE_RISK_FORM__CASE_DETAIL')")
    @GetMapping("/users/{username}/access/{crn}")
    fun checkAccess(@PathVariable username: String, @PathVariable crn: String): CaseAccess =
        userAccessService.caseAccessFor(username, crn)
}
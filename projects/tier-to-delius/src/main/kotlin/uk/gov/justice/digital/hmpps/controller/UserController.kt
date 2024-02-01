package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@RequestMapping("users")
@PreAuthorize("hasAnyRole('TIER_DETAILS','PROBATION_API__TIER__CASE_DETAIL')")
class UserController(private val userAccessService: UserAccessService) {

    @GetMapping("/{username}/access/{crn}")
    fun userAccessCheck(
        @PathVariable username: String,
        @PathVariable crn: String,
    ): CaseAccess = userAccessService.caseAccessFor(username, crn)
}

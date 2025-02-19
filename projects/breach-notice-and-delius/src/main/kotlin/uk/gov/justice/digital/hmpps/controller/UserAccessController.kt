package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@Tag(name = "User access")
class UserAccessController(private val userAccessService: UserAccessService) {
    @PreAuthorize("hasRole('PROBATION_API__BREACH_NOTICE__CASE_DETAIL')")
    @GetMapping("/users/{username}/access/{crn}")
    fun checkAccess(@PathVariable username: String, @PathVariable crn: String): CaseAccess =
        userAccessService.caseAccessFor(username, crn)
}
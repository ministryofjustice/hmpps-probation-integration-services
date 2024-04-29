package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@Tag(name = "User access")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class UserAccessController(private val userAccessService: UserAccessService) {
    @GetMapping("/user/{username}/access/{crn}")
    fun checkAccess(@PathVariable username: String, @PathVariable crn: String) =
        userAccessService.caseAccessFor(username, crn)
}
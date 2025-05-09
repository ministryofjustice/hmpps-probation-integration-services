package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.UserService

@Validated
@RestController
@RequestMapping("users/{username}")
class UserController(
    private val userService: UserService
) {
    @PreAuthorize("hasRole('PROBATION_API__FIND_AND_REFER__CASE_DETAIL')")
    @GetMapping("/access/{crn}")
    fun checkAccess(@PathVariable username: String, @PathVariable crn: String) =
        userService.caseAccessFor(username, crn)
}

package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.UserService

@RestController
@PreAuthorize("hasAnyRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL','PROBATION_API__MPOP__CASE_DETAIL')")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/user/{username}/homepage")
    @Operation(summary = "Get summary information for a user's homepage, including upcoming appointments and recent appointments requiring an outcome")
    fun getHomepage(@PathVariable username: String) = userService.getHomepage(username)
}

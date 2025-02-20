package uk.gov.justice.digital.hmpps.controller

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.model.DeliusRole
import uk.gov.justice.digital.hmpps.service.UserService

@RestController
@RequestMapping("users")
class UserController(private val userService: UserService) {
    @PreAuthorize("hasRole('PROBATION_API__PATHFINDER__USER_ROLES__RW')")
    @PutMapping(value = ["/{username}/roles/{roleName}"])
    fun addRole(@PathVariable username: String, @PathVariable roleName: String) =
        userService.addRole(username, roleName.deliusRole())

    @PreAuthorize("hasRole('PROBATION_API__PATHFINDER__USER_ROLES__RW')")
    @DeleteMapping(value = ["/{username}/roles/{roleName}"])
    fun removeRole(@PathVariable username: String, @PathVariable roleName: String) =
        userService.removeRole(username, roleName.deliusRole())

    @PreAuthorize("hasRole('PROBATION_API__PATHFINDER__USER_ROLES__RW')")
    @GetMapping(value = ["/{username}/details"])
    fun getDetails(@PathVariable username: String) = userService.getUserDetails(username)
}

private fun String.deliusRole() =
    DeliusRole.from(this) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Role Not Acceptable")

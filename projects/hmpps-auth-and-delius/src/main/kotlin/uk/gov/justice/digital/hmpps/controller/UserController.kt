package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.PasswordChangeRequest
import uk.gov.justice.digital.hmpps.service.UserService

@Validated
@RestController
@Tag(name = "User details")
class UserController(private val userService: UserService) {
    @GetMapping(value = ["/user/{username}"])
    @PreAuthorize("hasAnyRole('ROLE_DELIUS_USER_AUTH')")
    @Operation(description = "Get user details. Requires `ROLE_DELIUS_USER_AUTH`.")
    fun getUserDetails(@PathVariable username: String) = userService.getUserDetails(username)
        ?: throw NotFoundException("User", "username", username)

    @GetMapping(value = ["/user"])
    @PreAuthorize("hasAnyRole('ROLE_DELIUS_USER_AUTH')")
    @Operation(description = "Get users by email. Requires `ROLE_DELIUS_USER_AUTH`.")
    fun getUsersByEmail(@RequestParam email: String) = userService.getUsersByEmail(email)

    @PostMapping("/user/{username}/password")
    @PreAuthorize("hasRole('ROLE_DELIUS_USER_AUTH')")
    @Operation(description = "Change a Delius user's password. Requires `ROLE_DELIUS_USER_AUTH`.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Password changed successfully"),
            ApiResponse(responseCode = "404", description = "User not found")
        ]
    )
    fun changePassword(
        @PathVariable("username") @NotBlank username: String,
        @Valid @RequestBody
        request: PasswordChangeRequest
    ) = userService.changePassword(username, request.password)
}

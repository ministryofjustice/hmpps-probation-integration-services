package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.UserService

@RestController
@Tag(name = "Caseload Info")
@RequestMapping("/caseload/{username}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class UserController(private val userService: UserService) {

    @GetMapping
    @Operation(summary = "Gets caseloads for a person and staff within their teams")
    fun getCaseload(@PathVariable username: String) = userService.getUserDetails(username)
}
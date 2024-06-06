package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.UserService

@RestController
@Tag(name = "Caseload Info")
@RequestMapping("/caseload")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class CaseloadController(private val userService: UserService) {

    @GetMapping("/user/{username}")
    @Operation(summary = "Gets caseloads for the user")
    fun getUserCaseload(
        @PathVariable username: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "100") size: Int
    ) = userService.getUserCaseload(username, PageRequest.of(page, size))

    @GetMapping("/user/{username}/teams")
    @Operation(summary = "Gets the users teams")
    fun getUserTeams(@PathVariable username: String) = userService.getUserTeams(username)

    @GetMapping("/team/{teamCode}")

    @Operation(summary = "Gets the caseload for the team")
    fun getTeamCaseload(
        @PathVariable teamCode: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ) = userService.getTeamCaseload(teamCode, PageRequest.of(page, size))

    @GetMapping("/team/{teamCode}/staff")
    @Operation(summary = "Gets the staff within the team")
    fun getTeamStaff(@PathVariable teamCode: String) = userService.getTeamStaff(teamCode)
}
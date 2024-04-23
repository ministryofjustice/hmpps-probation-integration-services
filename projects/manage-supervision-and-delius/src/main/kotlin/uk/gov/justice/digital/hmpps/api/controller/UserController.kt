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
@RequestMapping("/caseload")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class UserController(private val userService: UserService) {

    @GetMapping("/user/{username}")
    @Operation(summary = "Gets caseloads for the user")
    fun getUserCaseload(@PathVariable username: String) = userService.getUserCaseload(username)

    @GetMapping("/user/{username}/teams")
    @Operation(summary = "Gets the users teams")
    fun getUserTeams(@PathVariable username: String) = userService.getUserTeams(username)

    @GetMapping("/team/{teamCode}/staff")
    @Operation(summary = "Gets the staff within the team")
    fun getTeamStaff(@PathVariable teamCode: String) = userService.getTeamStaff(teamCode)

    @GetMapping("/staff/{staffCode}")
    @Operation(summary = "Gets the caseload for a staff code ")
    fun getStaffCaseload(@PathVariable staffCode: String) = userService.getStaffCaseload(staffCode)
}
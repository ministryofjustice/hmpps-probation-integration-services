package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.user.UserSearchFilter
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.CaseloadOrderType
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

    @PostMapping("/user/{username}/search")
    @Operation(summary = "Gets caseloads for the user based on search filter")
    fun searchUserCaseload(
        @PathVariable username: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "100") size: Int,
        @RequestParam(required = false, defaultValue = "nextContact.desc") sortBy: String,
        @RequestBody body: UserSearchFilter
    ) = userService.searchUserCaseload(username, body, PageRequest.of(page, size, sort(sortBy)), sortBy)

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

    private fun sort(sortString: String): Sort {

        val regex = Regex(pattern = "[A-Z]+\\.(ASC|DESC)", options = setOf(RegexOption.IGNORE_CASE))
        if (!regex.matches(sortString)) {
            throw InvalidRequestException("Sort criteria invalid format")
        }
        val sortBy = sortString.split(".")[0].replace("(?<=.)[A-Z]".toRegex(), "_$0").uppercase()
        val direction = sortString.split(".")[1].uppercase()
        val sortType = runCatching { CaseloadOrderType.valueOf(sortBy) }.getOrNull()
            ?: throw InvalidRequestException("Sort by $sortString is not implemented")
        return Sort.by(Sort.Direction.valueOf(direction), sortType.sortColumn)
    }
}
package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.UserLocationService
import uk.gov.justice.digital.hmpps.service.UserService

@RestController
@Tag(name = "User")
@RequestMapping("/user/{username}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class UserController(
    private val userLocationService: UserLocationService,
    private val userService: UserService
) {

    @GetMapping("/locations")
    @Operation(summary = "Display user locations")
    fun getUserOfficeLocations(@PathVariable username: String) = userLocationService.getUserOfficeLocations(username)

    @GetMapping("/upcoming")
    @Operation(summary = "Gets caseloads for the user")
    fun getUserUpcomingAppointments(
        @PathVariable username: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "default") sortBy: String,
        @RequestParam(required = false, defaultValue = "true") ascending: Boolean
    ) = userService.getUpcomingAppointments(username, PageRequest.of(page, size, sort(sortBy, ascending)))

    private fun sort(sortString: String, ascending: Boolean): Sort {
        val direction = if (ascending) Sort.Direction.ASC else Sort.Direction.DESC
        return when (sortString) {
            "default" -> Sort.by(direction,"contact_date", "contact_start_time")
            else -> Sort.by(direction,"contact_date", "contact_start_time ")
        }
    }
}
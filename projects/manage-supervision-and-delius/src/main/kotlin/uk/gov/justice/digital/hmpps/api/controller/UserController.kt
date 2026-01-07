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

    @GetMapping("/schedule/upcoming")
    @Operation(summary = "Gets upcoming appointments for a user")
    fun getUserUpcomingAppointments(
        @PathVariable username: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "default") sortBy: String,
        @RequestParam(required = false, defaultValue = "true") ascending: Boolean
    ) = userService.getUpcomingAppointments(
        username,
        PageRequest.of(page, size, sortUpcomingAppointments(sortBy, ascending, true))
    )

    @GetMapping("/schedule/no-outcome")
    @Operation(summary = "Gets passed appointments without an outcome for a user")
    fun getUserAppointmentsWithoutOutcomes(
        @PathVariable username: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "date") sortBy: String,
        @RequestParam(required = false, defaultValue = "true") ascending: Boolean
    ) = userService.getAppointmentsWithoutOutcomes(
        username,
        PageRequest.of(page, size, sortAppointmentsWithoutOutcomes(sortBy, ascending, false))
    )

    @GetMapping("/appointments")
    @Operation(summary = "Gets passed appointments without an outcome for a user")
    fun getUserAppointments(
        @PathVariable username: String
    ) = userService.getAppointmentsForUser(username)

    private fun sortUpcomingAppointments(sortString: String, ascending: Boolean, offenderBased: Boolean): Sort {
        val direction = if (ascending) Sort.Direction.ASC else Sort.Direction.DESC
        val qualifier = if (offenderBased) "o." else ""
        return when (sortString) {
            "date" -> Sort.by(direction, "contact_date", "contact_start_time")
            "name" -> Sort.by(direction, "${qualifier}surname")
            "dob" -> Sort.by(direction, "${qualifier}date_of_birth_date")
            "appointment" -> Sort.by(direction, "rct.description")
            "sentence" -> Sort.by(direction, "nvl(rdt.description, latest_sentence_description)")
            else -> Sort.by(direction, "contact_date", "contact_start_time")
        }
    }

    private fun sortAppointmentsWithoutOutcomes(sortString: String, ascending: Boolean, offenderBased: Boolean): Sort {
        val direction = if (ascending) Sort.Direction.ASC else Sort.Direction.DESC
        val qualifier = if (offenderBased) "o." else ""
        return when (sortString) {
            "date" -> Sort.by(direction, "contact_date", "contact_start_time")
            "name" -> Sort.by(direction, "${qualifier}surname")
            "dob" -> Sort.by(direction, "${qualifier}date_of_birth_date")
            "appointment" -> Sort.by(direction, "rct.description")
            "sentence" -> Sort.by(
                direction, "case when d.disposal_id is not null \n" +
                    "                    then \n" +
                    "                        rdt.description\n" +
                    "                    else\n" +
                    "                        (select rdt.description\n" +
                    "                          from disposal d\n" +
                    "                          join r_disposal_type rdt on rdt.disposal_type_id = d.disposal_type_id\n" +
                    "                          where d.offender_id = o.offender_id\n" +
                    "                          order by e.created_datetime desc fetch first 1 row only)\n" +
                    "                    end"
            )

            else -> Sort.by(direction, "contact_date", "contact_start_time")
        }
    }

    @GetMapping("/providers")
    fun getUserProviders(
        @PathVariable username: String,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) team: String?
    ) = userService.getProvidersForUser(username, region, team)
}
package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Sort
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ScheduleService

@RestController
@Tag(name = "Schedule")
@RequestMapping("/schedule/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class ScheduleController(private val scheduleService: ScheduleService) {

    @GetMapping("/upcoming")
    @Operation(summary = "Gets upcoming schedule information’ ")
    fun getUpcomingSchedule(
        @PathVariable crn: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "date") sortBy: String,
        @RequestParam(required = false, defaultValue = "true") ascending: Boolean
    ) = scheduleService.getPersonUpcomingSchedule(crn, PageRequest.of(page, size, sort(sortBy, ascending)))

    @GetMapping("/previous")
    @Operation(summary = "Gets previous schedule information’ ")
    fun getPreviousSchedule(
        @PathVariable crn: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "date") sortBy: String,
        @RequestParam(required = false, defaultValue = "false") ascending: Boolean
    ) = scheduleService.getPersonPreviousSchedule(crn, PageRequest.of(page, size, sort(sortBy, ascending)))

    @GetMapping("/appointment/{contactId}")
    @Operation(summary = "Gets individual appointment information’ ")
    fun getContact(@PathVariable crn: String, @PathVariable contactId: Long) =
        scheduleService.getPersonAppointment(crn, contactId)

    @GetMapping("/appointment/{contactId}/note/{noteId}")
    @Operation(summary = "Gets individual appointment information’ ")
    fun getContactNote(@PathVariable crn: String, @PathVariable contactId: Long, @PathVariable noteId: Int) =
        scheduleService.getPersonAppointment(crn, contactId, noteId)

    @GetMapping("/next-appointment")
    @Operation(summary = "Gets the next appointment in the future and after the date of the passed in contact")
    fun getContactNote(@PathVariable crn: String, @RequestParam contactId: Long, @RequestParam username: String) =
        scheduleService.getNextComAppointment(crn, contactId, username)
}

private fun sort(sortString: String, ascending: Boolean): Sort {
    val direction = if (ascending) Sort.Direction.ASC else Sort.Direction.DESC
    return when (sortString) {
        "date" -> Sort.by(direction, "contact_date", "contact_start_time")
        "appointment" -> Sort.by(direction, "description")
        else -> Sort.by(direction, "contact_date", "contact_start_time")
    }
}
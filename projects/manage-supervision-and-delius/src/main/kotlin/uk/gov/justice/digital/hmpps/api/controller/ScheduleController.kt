package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ScheduleService

@RestController
@Tag(name = "Sentence")
@RequestMapping("/schedule/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class ScheduleController(private val scheduleService: ScheduleService) {

    @GetMapping("/upcoming")
    @Operation(summary = "Gets upcoming schedule information’ ")
    fun getUpcomingSchedule(@PathVariable crn: String) = scheduleService.getPersonUpcomingSchedule(crn)

    @GetMapping("/previous")
    @Operation(summary = "Gets previous schedule information’ ")
    fun getPreviousSchedule(@PathVariable crn: String) = scheduleService.getPersonPreviousSchedule(crn)

    @GetMapping("/appointment/{contactId}")
    @Operation(summary = "Gets individual appointment information’ ")
    fun getContact(@PathVariable crn: String, @PathVariable contactId: Long) =
        scheduleService.getPersonAppointment(crn, contactId)
}
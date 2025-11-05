package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.AppointmentsService

@RestController
@RequestMapping("/projects")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class AppointmentsController(
    private val appointmentsService: AppointmentsService
) {
    @GetMapping(value = ["/{projectCode}/appointments/{appointmentId}"])
    fun getAppointment(@PathVariable projectCode: String, @PathVariable appointmentId: Long,
        @RequestParam username: String) = appointmentsService.getAppointment(projectCode, appointmentId, username)
}
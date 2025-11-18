package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.AppointmentOutcomeRequest
import uk.gov.justice.digital.hmpps.service.AppointmentsService
import java.time.LocalDate
import java.time.LocalTime

@RestController
@RequestMapping("/projects")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class AppointmentsController(
    private val appointmentsService: AppointmentsService
) {
    @GetMapping(value = ["/{projectCode}/appointments/{appointmentId}"])
    fun getAppointment(
        @PathVariable projectCode: String, @PathVariable appointmentId: Long,
        @RequestParam username: String
    ) = appointmentsService.getAppointment(projectCode, appointmentId, username)

    @GetMapping(value = ["/{projectCode}/appointments"])
    fun getSession(
        @PathVariable projectCode: String, @RequestParam date: LocalDate,
        @RequestParam startTime: LocalTime, @RequestParam endTime: LocalTime, @RequestParam username: String
    ) = appointmentsService.getSession(projectCode, date, startTime, endTime, username)

    @PutMapping(value = ["/{projectCode}/appointments/{appointmentId}/outcome"])
    fun updateAppointmentOutcome(
        @PathVariable projectCode: String, @PathVariable appointmentId: Long,
        @RequestBody appointmentOutcome: AppointmentOutcomeRequest
    ) = appointmentsService.updateAppointmentOutcome(projectCode, appointmentId, appointmentOutcome)
}
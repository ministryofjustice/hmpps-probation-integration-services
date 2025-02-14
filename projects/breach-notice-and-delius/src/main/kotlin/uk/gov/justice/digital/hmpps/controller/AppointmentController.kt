package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.AppointmentService

@RestController
class AppointmentController(private val appointmentService: AppointmentService) {
    @PreAuthorize("hasRole('PROBATION_API__BREACH_NOTICE__CASE_DETAIL')")
    @GetMapping(value = ["/next-appointment-details/{crn}"])
    fun getFutureAppointments(@PathVariable crn: String) = appointmentService.getNextAppointmentDetails(crn)
}
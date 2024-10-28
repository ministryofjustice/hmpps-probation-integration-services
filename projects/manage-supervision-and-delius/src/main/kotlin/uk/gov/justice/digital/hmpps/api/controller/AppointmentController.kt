package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.service.AppointmentService

@RestController
@Tag(name = "Sentence")
@RequestMapping("/appointments/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class AppointmentController (private val appointmentService: AppointmentService) {

    @PostMapping
    fun createAppointment(@PathVariable crn: String, @RequestBody createAppointment: CreateAppointment) =
        appointmentService.createAppointment(crn, createAppointment)
}
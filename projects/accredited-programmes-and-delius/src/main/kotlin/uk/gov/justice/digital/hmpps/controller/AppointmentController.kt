package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.GetAppointmentsRequest
import uk.gov.justice.digital.hmpps.service.AppointmentService

@RestController
@Tag(name = "Appointments")
@PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__CASE_DETAIL')")
class AppointmentController(private val appointmentService: AppointmentService) {
    @Operation(summary = "Get all accredited programmes appointments for a given group of cases within the provided data range")
    @PostMapping(value = ["/appointments/search"])
    fun searchAppointments(@RequestBody request: GetAppointmentsRequest) =
        appointmentService.getAppointments(request)
}

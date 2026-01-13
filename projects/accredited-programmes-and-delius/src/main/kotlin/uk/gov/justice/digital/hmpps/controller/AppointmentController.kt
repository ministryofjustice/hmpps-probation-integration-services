package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.CreateAppointmentsRequest
import uk.gov.justice.digital.hmpps.model.DeleteAppointmentsRequest
import uk.gov.justice.digital.hmpps.model.GetAppointmentsRequest
import uk.gov.justice.digital.hmpps.model.UpdateAppointmentsRequest
import uk.gov.justice.digital.hmpps.service.AccreditedProgrammesAppointmentService

@RestController
@Tag(name = "Appointments")
@RequestMapping("appointments")
@PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__CASE_DETAIL')")
class AppointmentController(private val appointmentService: AccreditedProgrammesAppointmentService) {
    @Operation(summary = "Get all accredited programmes appointments for a given group of cases within the provided data range")
    @PostMapping(value = ["/search"])
    fun searchAppointments(@Valid @RequestBody request: GetAppointmentsRequest) =
        appointmentService.getAppointments(request)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAppointments(@Valid @RequestBody request: CreateAppointmentsRequest) {
        appointmentService.create(request)
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateAppointments(@Valid @RequestBody request: UpdateAppointmentsRequest) {
        appointmentService.update(request)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAppointments(@Valid @RequestBody request: DeleteAppointmentsRequest) {
        appointmentService.delete(request)
    }
}

package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.appointment.RecreateAppointmentRequest
import uk.gov.justice.digital.hmpps.api.model.appointment.RescheduleAppointmentRequest
import uk.gov.justice.digital.hmpps.aspect.WithDeliusUser
import uk.gov.justice.digital.hmpps.service.RecreateAppointment
import uk.gov.justice.digital.hmpps.service.RescheduleAppointment

@RestController
@Tag(name = "Sentence")
@RequestMapping("/appointments")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class AppointmentsController(
    private val rescheduleAppointment: RescheduleAppointment,
    private val recreateAppointment: RecreateAppointment,
) {
    @PutMapping("/{id}/reschedule")
    @WithDeliusUser
    fun rescheduleAppointment(
        @PathVariable id: Long,
        @Valid @RequestBody request: RescheduleAppointmentRequest
    ) {
        rescheduleAppointment.reschedule(id, request)
    }

    @PutMapping("/{id}/recreate")
    @WithDeliusUser
    fun recreateAppointment(
        @PathVariable id: Long,
        @Valid @RequestBody request: RecreateAppointmentRequest
    ) {
        recreateAppointment.recreate(id, request)
    }
}
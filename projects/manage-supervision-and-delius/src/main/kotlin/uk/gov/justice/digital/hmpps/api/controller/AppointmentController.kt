package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.Outcome
import uk.gov.justice.digital.hmpps.service.AppointmentOutcomeService
import uk.gov.justice.digital.hmpps.service.SentenceAppointmentService

@RestController
@Tag(name = "Sentence")
@RequestMapping("/appointments/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class AppointmentController(
    private val appointmentService: SentenceAppointmentService,
    private val appointmentOutcomeService: AppointmentOutcomeService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAppointment(@PathVariable crn: String, @RequestBody createAppointment: CreateAppointment) =
        appointmentService.createAppointment(crn, createAppointment)

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun recordOutcome(@RequestBody outcome: Outcome) = appointmentOutcomeService.recordOutcome(outcome)
}
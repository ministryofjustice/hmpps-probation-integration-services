package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.OfficeLocationRequest
import uk.gov.justice.digital.hmpps.api.model.appointment.Outcome
import uk.gov.justice.digital.hmpps.service.AppointmentOutcomeService
import uk.gov.justice.digital.hmpps.service.AppointmentService
import uk.gov.justice.digital.hmpps.service.SentenceAppointmentService
import uk.gov.justice.digital.hmpps.service.UserLocationService

@RestController
@Tag(name = "Sentence")
@RequestMapping("/appointment")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class AppointmentController(
    private val sentenceAppointmentService: SentenceAppointmentService,
    private val appointmentOutcomeService: AppointmentOutcomeService,
    private val userLocationService: UserLocationService,
    private val appointmentService: AppointmentService
) {

    @PostMapping("/{crn}")
    @ResponseStatus(HttpStatus.CREATED)
    fun createAppointment(@PathVariable crn: String, @Valid @RequestBody createAppointment: CreateAppointment) =
        sentenceAppointmentService.createAppointment(crn, createAppointment)

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    fun recordOutcome(@RequestBody outcome: Outcome) = appointmentOutcomeService.recordOutcome(outcome)

    @GetMapping("/{crn}/contact-type/{code}")
    fun getProbationRecordsByContactType(@PathVariable crn: String, @PathVariable code: String) =
        appointmentService.getProbationRecordsByContactType(crn, code)

    @GetMapping("/types")
    fun getAppointmentTypes() = appointmentService.getAppointmentTypes()

    @GetMapping("/teams/provider/{code}")
    fun getTeamsByProvider(@PathVariable code: String) = appointmentService.getTeamsByProvider(code)

    @GetMapping("/location")
    fun getOfficeLocationByTeamAndProvider(@RequestBody locationRequest: OfficeLocationRequest) =
        appointmentService.getOfficeByProviderAndTeam(locationRequest)

    @GetMapping("/staff/team/{code}")
    fun getStaffByTeam(@PathVariable code: String) =
        userLocationService.getStaffByTeam(code)
}
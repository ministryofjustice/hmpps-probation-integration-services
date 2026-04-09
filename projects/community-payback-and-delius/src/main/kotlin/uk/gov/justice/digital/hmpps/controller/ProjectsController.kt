package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.UpdateAppointmentRequest
import uk.gov.justice.digital.hmpps.model.CreateAppointmentsRequest
import uk.gov.justice.digital.hmpps.service.CommunityPaybackAppointmentsService
import uk.gov.justice.digital.hmpps.service.ProjectService
import java.time.LocalDate

@Validated
@RestController
@RequestMapping("/projects")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class ProjectsController(
    private val projectService: ProjectService,
    private val communityPaybackAppointmentsService: CommunityPaybackAppointmentsService
) {
    @GetMapping(value = ["/{projectCode}"])
    fun getProject(@PathVariable projectCode: String) = projectService.getProject(projectCode)

    @GetMapping(value = ["/{projectCode}/appointments/{appointmentId}"])
    fun getAppointment(
        @PathVariable projectCode: String,
        @PathVariable appointmentId: Long,
        @RequestParam username: String
    ) = communityPaybackAppointmentsService.getAppointment(projectCode, appointmentId, username)

    @GetMapping(value = ["/{projectCode}/appointments"])
    fun getSession(
        @PathVariable projectCode: String,
        @RequestParam date: LocalDate,
        @RequestParam username: String
    ) = communityPaybackAppointmentsService.getSession(projectCode, date, username)

    @PutMapping(value = ["/{projectCode}/appointments/{appointmentId}"])
    fun updateAppointment(
        @PathVariable projectCode: String,
        @PathVariable appointmentId: Long,
        @RequestBody appointmentOutcome: UpdateAppointmentRequest
    ) = communityPaybackAppointmentsService.updateAppointment(projectCode, appointmentId, appointmentOutcome)

    @Operation(
        deprecated = true,
        description = "Deprecated, should instead use PUT /projects/{projectCode}/appointments/{appointmentId}",
    )
    @PutMapping(value = ["/{projectCode}/appointments/{appointmentId}/outcome"])
    fun updateAppointmentOutcome(
        @PathVariable projectCode: String,
        @PathVariable appointmentId: Long,
        @RequestBody appointmentOutcome: UpdateAppointmentRequest
    ) = communityPaybackAppointmentsService.updateAppointment(projectCode, appointmentId, appointmentOutcome)

    @PostMapping(value = ["/{projectCode}/appointments"])
    fun createAppointments(
        @PathVariable projectCode: String,
        @Valid @RequestBody request: CreateAppointmentsRequest
    ) = communityPaybackAppointmentsService.createAppointments(projectCode, request)
}
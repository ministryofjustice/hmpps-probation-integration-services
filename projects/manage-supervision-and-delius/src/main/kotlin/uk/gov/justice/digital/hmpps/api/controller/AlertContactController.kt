package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.user.ClearAlerts
import uk.gov.justice.digital.hmpps.api.model.user.UserAlerts
import uk.gov.justice.digital.hmpps.aspect.WithDeliusUser
import uk.gov.justice.digital.hmpps.service.UserAlertService

@RestController
@Tag(name = "Activity")
@RequestMapping("/alerts")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class AlertContactController(private val userAlertService: UserAlertService) {
    @GetMapping
    @WithDeliusUser
    @Operation(summary = "Get all alerts for the current user")
    fun getUserAlerts(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "DATE_AND_TIME,desc") sort: String,
    ): UserAlerts {
        val sortParts = sort.split(",")
        val properties = AppointmentSort.entries.first { it.name == sortParts[0].uppercase() }.properties
        val direction = if (sortParts.size == 2) {
            Sort.Direction.valueOf(sortParts[1].uppercase())
        } else {
            Sort.Direction.DESC
        }
        return userAlertService.getUserAlerts(
            PageRequest.of(
                page,
                size,
                Sort.by(direction, *properties.toTypedArray())
            )
        )
    }

    @PutMapping
    @WithDeliusUser
    @Operation(summary = "Allows a user to clear alerts")
    fun clearUserAlerts(@Valid @RequestBody toClear: ClearAlerts) {
        userAlertService.clearAlerts(toClear)
    }

    @GetMapping("/{alertId}/notes/{noteId}")
    @Operation(summary = "Get alert note")
    fun getAlertNote(
        @PathVariable alertId: Long,
        @PathVariable noteId: Int
    ) = userAlertService.getAlertNote(alertId, noteId)
}

enum class AppointmentSort(val properties: List<String>) {
    DATE_AND_TIME(listOf("c.date", "c.startTime")),
    SURNAME(listOf("c.person.surname")),
    TYPE_DESCRIPTION(listOf("c.type.description"))
}
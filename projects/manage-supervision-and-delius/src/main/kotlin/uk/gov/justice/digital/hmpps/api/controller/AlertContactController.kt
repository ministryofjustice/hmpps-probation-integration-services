package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): UserAlerts = userAlertService.getUserAlerts(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date")))
}
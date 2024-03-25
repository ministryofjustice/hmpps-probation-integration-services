package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.OverviewService

@Validated
@RestController
@Tag(name = "Overview")
@RequestMapping("/overview/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class OverviewController(private val overviewService: OverviewService) {

    @GetMapping
    @Operation(summary = "MAS Overview containing Personal Details, Activity, Compliance, Schedule, Orders and Sentences ")
    fun getOverview(@PathVariable crn: String) = overviewService.getOverview(crn)
}

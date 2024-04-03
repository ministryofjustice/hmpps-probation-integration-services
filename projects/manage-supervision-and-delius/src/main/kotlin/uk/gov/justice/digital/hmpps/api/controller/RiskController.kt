package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.RiskService

@RestController
@Tag(name = "Risk Flags")
@RequestMapping("/risk-flags/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class RiskController(private val riskService: RiskService) {

    @GetMapping
    @Operation(summary = "Gets all risk flags for an offender’ ")
    fun getPersonRiskFlags(@PathVariable crn: String) = riskService.getPersonRiskFlags(crn)

    @GetMapping("/{riskFlagId}")
    @Operation(summary = "Gets an individual risk flag for an offender’ ")
    fun getPersonRiskFlag(@PathVariable crn: String, @PathVariable riskFlagId: Long) =
        riskService.getPersonRiskFlag(crn, riskFlagId)
}
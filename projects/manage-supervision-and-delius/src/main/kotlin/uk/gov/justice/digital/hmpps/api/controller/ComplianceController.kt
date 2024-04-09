package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ComplianceService

@RestController
@Tag(name = "Compliance")
@RequestMapping("/compliance/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class ComplianceController(private val complianceService: ComplianceService) {

    @GetMapping
    @Operation(summary = "Gets all compliance info for a person")
    fun getPersonCompliance(@PathVariable crn: String) = complianceService.getPersonCompliance(crn)
}
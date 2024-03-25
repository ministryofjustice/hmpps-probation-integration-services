package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.RiskSummary
import uk.gov.justice.digital.hmpps.service.RegistrationService

@RestController
@RequestMapping("/registrations/{crn}")
class RegistrationResource(private val registrationService: RegistrationService) {

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @GetMapping("/flags")
    fun getRiskFlagsByCrn(@PathVariable crn: String): RiskSummary = registrationService.findActiveRegistrations(crn)
}
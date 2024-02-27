package uk.gov.justice.digital.hmpps.api

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.Registrations
import uk.gov.justice.digital.hmpps.service.RegistrationService

@RestController
@RequestMapping("probation-cases/{crn}")
class RegistrationResource(private val registrationService: RegistrationService) {
    @PreAuthorize("hasRole('PROBATION_API__OASYS__CASE_DETAIL')")
    @GetMapping(value = ["/registrations"])
    fun handle(@PathVariable("crn") crn: String): Registrations = registrationService.findActiveRegistrations(crn)
}



package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.integrations.delius.service.RegistrationService

@RestController
@RequestMapping("probation-case/{crn}/registrations")
@PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
class RegistrationResource(private val registrationService: RegistrationService) {

    @GetMapping
    fun getOffenderRegistrations(
        @PathVariable crn: String,
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean,
    ) = registrationService.getRegistrationsFor(crn, activeOnly)
}

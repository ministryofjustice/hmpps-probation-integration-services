package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.model.BreachDetails
import uk.gov.justice.digital.hmpps.integrations.delius.service.BreachDetailsService

@RestController
class DomainEventsController(
    private val breachDetailsService: BreachDetailsService
) {
    @PreAuthorize("hasRole('ROLE_DELIUS_DOMAIN_EVENTS')")
    @GetMapping(
        value = [
            "/details/enforcement.breach.raised/{nsiId}",
            "/details/enforcement.breach.concluded/{nsiId}"
        ]
    )
    fun getBreachDetails(
        @PathVariable("nsiId") nsiId: Long
    ): BreachDetails {
        return breachDetailsService.getBreachDetails(nsiId)
    }
}

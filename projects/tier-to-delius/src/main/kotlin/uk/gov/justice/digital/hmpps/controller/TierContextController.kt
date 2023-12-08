package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TierContextController(private val tierDetailsService: TierDetailsService) {
    @GetMapping(value = ["/tier-details/{crn}"])
    @PreAuthorize("hasRole('TIER_DETAILS')")
    fun tierContext(
        @PathVariable crn: String,
    ) = tierDetailsService.tierDetails(crn)
}

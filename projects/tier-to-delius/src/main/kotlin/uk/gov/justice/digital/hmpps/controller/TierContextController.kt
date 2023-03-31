package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TierContextController() {
    @GetMapping(value = ["/tier-context"])
    @PreAuthorize("hasRole('TIER_DETAILS')")
    fun tierContext() = "success"
}

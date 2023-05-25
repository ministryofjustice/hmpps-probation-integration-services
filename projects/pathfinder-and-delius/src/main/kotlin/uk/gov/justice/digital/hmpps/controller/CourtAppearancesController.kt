package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CourtAppearanceService

@RestController
class CourtAppearancesController(private val courtAppearanceService: CourtAppearanceService) {
    @PreAuthorize("hasRole('ROLE_PATHFINDER_PROBATION_CASE')")
    @GetMapping(value = ["/court-appearances"])
    fun courtAppearances() =
        courtAppearanceService.getCourtAppearances()
}

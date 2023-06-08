package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.BatchRequest
import uk.gov.justice.digital.hmpps.service.CourtAppearanceService

@RestController
class CourtAppearancesController(private val courtAppearanceService: CourtAppearanceService) {
    @PreAuthorize("hasRole('ROLE_PATHFINDER_PROBATION_CASE')")
    @PostMapping(value = ["/court-appearances"])
    fun courtAppearances(
        @Valid @RequestBody
        request: BatchRequest
    ) = courtAppearanceService.getCourtAppearances(request)
}

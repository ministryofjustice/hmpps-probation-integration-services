package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CourtAppearanceService
import java.time.LocalDate

@RestController
class CourtAppearancesController(private val courtAppearanceService: CourtAppearanceService) {
    @PreAuthorize("hasRole('ROLE_SOC_PROBATION_CASE')")
    @GetMapping(value = ["/court-appearances/{value}"])
    fun courtAppearances(
        @PathVariable value: String,
        @RequestParam(required = false, defaultValue = "CRN") type: IdentifierType,
        @RequestParam(required = false) fromDate: LocalDate?,
    ) = courtAppearanceService.getCourtAppearances(value, type, fromDate)
}

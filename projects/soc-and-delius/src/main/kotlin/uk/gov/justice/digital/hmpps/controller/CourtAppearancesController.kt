package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.CourtAppearanceService
import java.time.LocalDate

@RestController
@PreAuthorize("hasRole('PROBATION_API__SOC__CASE_DETAIL')")
class CourtAppearancesController(private val courtAppearanceService: CourtAppearanceService) {
    @GetMapping(value = ["/court-appearances/{value}"])
    fun courtAppearances(
        @PathVariable value: String,
        @RequestParam(required = false, defaultValue = "CRN") type: IdentifierType,
        @RequestParam(required = false) fromDate: LocalDate?
    ) = courtAppearanceService.getCourtAppearances(value, type, fromDate)

    @Operation(description = "Get all court appearances from today onwards, for a list of up to 500 CRNs")
    @PostMapping(value = ["/court-appearances"])
    fun courtAppearances(
        @RequestBody @Size(min = 1, max = 500, message = "Please provide between 1 and 500 CRNs") crns: List<String>
    ) = courtAppearanceService.getAllCourtAppearances(crns)
}

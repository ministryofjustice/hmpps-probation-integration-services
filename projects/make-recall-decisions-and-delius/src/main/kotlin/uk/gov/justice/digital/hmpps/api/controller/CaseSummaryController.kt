package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseSummaryService

@RestController
@Tag(name = "Case Summary")
@RequestMapping("/case-summary/{crn}")
@PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISIONS_API')")
class CaseSummaryController(private val caseSummaryService: CaseSummaryService) {

    @GetMapping(value = ["/personal-details"])
    @Operation(summary = "Personal details including name, date of birth, address")
    fun getPersonalDetails(@PathVariable("crn") crn: String) = caseSummaryService.getPersonalDetails(crn)

    @GetMapping(value = ["/overview"])
    @Operation(summary = "Overview of the probation case including active events/convictions, register flags")
    fun getOverview(@PathVariable("crn") crn: String) = caseSummaryService.getOverview(crn)

    @GetMapping(value = ["/mappa-and-rosh-history"])
    @Operation(
        summary = "Current MAPPA and historical RoSH registrations",
        description = "<p>This is intended to populate the MAPPA (Multi-agency public protection arrangements) and RoSH (Risk of Serious Harm) widgets." +
            "<p>Note that risk assessment information is generally held in OASys, and this endpoint only surfaces supplementary Delius registration/register details."
    )
    fun getMappaAndRoshHistory(@PathVariable("crn") crn: String) = caseSummaryService.getMappaAndRoshHistory(crn)
}

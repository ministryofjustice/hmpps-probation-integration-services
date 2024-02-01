package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.PastOrPresent
import org.hibernate.validator.constraints.Length
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseSummaryService
import java.time.LocalDate

@Validated
@RestController
@Tag(name = "Case Summary")
@RequestMapping("/case-summary/{crn}")
@PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISIONS_API','PROBATION_API__CONSIDER_A_RECALL__CASE_DETAIL')")
class CaseSummaryController(private val caseSummaryService: CaseSummaryService) {

    @GetMapping(value = ["/personal-details"])
    @Operation(summary = "Personal details including name, date of birth, address")
    fun getPersonalDetails(@PathVariable crn: String) = caseSummaryService.getPersonalDetails(crn)

    @GetMapping(value = ["/overview"])
    @Operation(summary = "Overview of the probation case including active events/convictions, register flags")
    fun getOverview(@PathVariable crn: String) = caseSummaryService.getOverview(crn)

    @GetMapping(value = ["/mappa-and-rosh-history"])
    @Operation(
        summary = "Current MAPPA and historical RoSH registrations",
        description = "<p>This is intended to populate the MAPPA (Multi-agency public protection arrangements) and RoSH (Risk of Serious Harm) widgets." +
            "<p>Note that risk assessment information is generally held in OASys, and this endpoint only surfaces supplementary Delius registration/register details."
    )
    fun getMappaAndRoshHistory(@PathVariable crn: String) = caseSummaryService.getMappaAndRoshHistory(crn)

    @GetMapping(value = ["/licence-conditions"])
    @Operation(
        summary = "Active events/convictions, with licence conditions",
        description = "Only returns active licence conditions that are associated with active events/convictions"
    )
    fun getLicenceConditions(@PathVariable crn: String) = caseSummaryService.getLicenceConditions(crn)

    @GetMapping(value = ["/contact-history"])
    @Operation(
        summary = "Contact history",
        description = "Returns matching historical records from the Delius contact log, including linked documents, and a summary showing the total number of contacts on the case grouped by type."
    )
    fun getContactHistory(
        @PathVariable
        crn: String,
        @Parameter(description = "Search for contacts that contain the provided text. This currently performs a simple substring match against the notes.")
        @RequestParam(required = false)
        @Length(max = 100)
        query: String?,
        @Parameter(description = "Return only contacts that start after this date")
        @RequestParam(required = false)
        from: LocalDate?,
        @Parameter(description = "Return only contacts that start before this date. Defaults to the current date. If provided, this value must be on or before the current date.")
        @RequestParam(required = false)
        @PastOrPresent
        to: LocalDate?,
        @Parameter(description = "Filter on contact type codes")
        @RequestParam(defaultValue = "")
        type: List<String> = listOf(),
        @Parameter(description = "Include (true) or exclude (false) any contact types that are system-generated")
        @RequestParam(defaultValue = "true")
        includeSystemGenerated: Boolean = true
    ) = caseSummaryService.getContactHistory(crn, query, from, to ?: LocalDate.now(), type, includeSystemGenerated)

    @GetMapping(value = ["/recommendation-model"])
    @Operation(
        summary = "Case data required to populate the recommendation model",
        description = "This is used to populate the Recall Part A document template when a recommendation is made, and to refresh the case data when a recall decision is made later."
    )
    fun getRecommendationModel(@PathVariable crn: String) = caseSummaryService.getRecommendationModel(crn)
}

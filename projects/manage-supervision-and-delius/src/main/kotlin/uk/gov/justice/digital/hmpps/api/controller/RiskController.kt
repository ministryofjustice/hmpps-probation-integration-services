package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.RiskService

@RestController
@Tag(name = "Risk Flags")
@RequestMapping("/risk-flags/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class RiskController(private val riskService: RiskService) {

    @GetMapping
    @Operation(summary = "Gets all risk flags for an offender’ ")
    fun getPersonRiskFlags(@PathVariable crn: String) = riskService.getPersonRiskFlags(crn)

    @GetMapping("/{riskFlagId}")
    @Operation(summary = "Gets an individual risk flag for an offender’ ")
    fun getPersonRiskFlag(
        @PathVariable crn: String,
        @PathVariable riskFlagId: Long,
    ) = riskService.getPersonRiskFlag(crn, riskFlagId)

    @GetMapping("/{riskFlagId}/note/{noteId}")
    @Operation(summary = "Gets an individual risk flag for an offender’ ")
    fun getPersonRiskFlagSingleNote(@PathVariable crn: String, @PathVariable riskFlagId: Long, @PathVariable noteId: Int) =
        riskService.getPersonRiskFlag(crn, riskFlagId, noteId = noteId)

    @GetMapping("/{riskFlagId}/risk-removal-note/{riskRemovalNoteId}")
    @Operation(summary = "Gets an individual risk flag for an offender’ ")
    fun getPersonRiskFlagRemovalHistorySingleNote(@PathVariable crn: String, @PathVariable riskFlagId: Long, @PathVariable riskRemovalNoteId: Int) =
        riskService.getPersonRiskFlag(crn, riskFlagId, riskRemovalNoteId = riskRemovalNoteId)
}
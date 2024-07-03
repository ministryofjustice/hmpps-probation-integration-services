package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotEmpty
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.integrations.delius.service.ConvictionService
import uk.gov.justice.digital.hmpps.integrations.delius.service.InterventionService
import uk.gov.justice.digital.hmpps.integrations.delius.service.RequirementService

@RestController
@RequestMapping("probation-case/{crn}/convictions")
@PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
class ConvictionResource(
    private val convictionService: ConvictionService,
    private val requirementService: RequirementService,
    private val interventionService: InterventionService,
) {

    @GetMapping
    fun getConvictionsForOffenderByCrn(
        @PathVariable crn: String,
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean,
    ) = convictionService.convictionFor(crn, activeOnly)

    @GetMapping("/{convictionId}")
    fun getConvictionForOffenderByCrnAndConvictionId(
        @PathVariable crn: String,
        @PathVariable convictionId: Long
    ) = convictionService.getConvictionFor(crn, convictionId)

    @GetMapping("/{convictionId}/requirements")
    fun getRequirementsForConviction(
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
        @RequestParam(required = false, defaultValue = "true") activeOnly: Boolean,
        @RequestParam(required = false, defaultValue = "true") excludeSoftDeleted: Boolean
    ) = requirementService.getRequirementsByConvictionId(crn, convictionId, activeOnly, !excludeSoftDeleted)

    @GetMapping("/{convictionId}/nsis")
    fun getNsisByCrnAndConvictionId(
        @Parameter(name = "crn", description = "CRN for the offender", example = "A123456", required = true)
        @PathVariable crn: String,
        @Parameter(
            name = "convictionId",
            description = "ID for the conviction / event",
            example = "2500295345",
            required = true
        )
        @PathVariable convictionId: Long,
        @Parameter(
            name = "nsiCodes",
            description = "list of NSI codes to constrain by",
            example = "BRE,BRES",
            required = true
        )
        @NotEmpty @RequestParam(required = true) nsiCodes: List<String>
    ) = interventionService.getNsiByCodes(crn, convictionId, nsiCodes)
}

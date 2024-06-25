package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.integrations.delius.service.ConvictionService
import uk.gov.justice.digital.hmpps.integrations.delius.service.RequirementService

@RestController
@RequestMapping("probation-case/{crn}/convictions")
@PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
class ConvictionResource(
    private val convictionService: ConvictionService,
    private val requirementService: RequirementService,
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
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean,
        @RequestParam(required = false, defaultValue = "false") excludeSoftDeleted: Boolean,
    ) = requirementService.getRequirementsByConvictionId(crn, convictionId, activeOnly, excludeSoftDeleted)
}

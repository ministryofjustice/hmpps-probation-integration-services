package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotEmpty
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.integrations.delius.service.*

@RestController
@RequestMapping("probation-case/{crn}/convictions")
@PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
class ConvictionResource(
    private val convictionService: ConvictionService,
    private val requirementService: RequirementService,
    private val interventionService: InterventionService,
    private val attendanceService: AttendanceService,
    private val courtAppearanceService: CourtAppearanceService
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

    @GetMapping("/{convictionId}/nsis/{nsiId}")
    fun getNsiByNsiId(
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
            name = "nsiId",
            description = "ID for the nsi",
            example = "2500295123",
            required = true
        )
        @PathVariable nsiId: Long
    ) = interventionService.getNsiByNsiId(crn, convictionId, nsiId)

    @GetMapping("/{convictionId}/pssRequirements")
    fun getPssRequirementsByConvictionId(
        @PathVariable crn: String,
        @PathVariable convictionId: Long
    ) = requirementService.getPssRequirementsByConvictionId(crn, convictionId)

    @GetMapping("/{convictionId}/attendancesFilter")
    fun getConvictionAttendances(
        @PathVariable crn: String,
        @PathVariable convictionId: Long
    ) = attendanceService.getAttendancesFor(crn, convictionId)

    @GetMapping("/{convictionId}/courtAppearances")
    fun getConvictionCourtAppearances(
        @PathVariable crn: String,
        @PathVariable convictionId: Long
    ) = courtAppearanceService.getCourtAppearancesFor(crn, convictionId)
}

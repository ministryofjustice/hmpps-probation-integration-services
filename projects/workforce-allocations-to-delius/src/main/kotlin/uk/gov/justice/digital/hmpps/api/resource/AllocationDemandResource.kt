package uk.gov.justice.digital.hmpps.api.resource

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandRequest
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandResponse
import uk.gov.justice.digital.hmpps.service.AllocationDemandService
import uk.gov.justice.digital.hmpps.service.AllocationRiskService

@Validated
@RestController
@RequestMapping("/allocation-demand")
class AllocationDemandResource(
    private val allocationDemand: AllocationDemandService,
    private val allocationRisk: AllocationRiskService
) {

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @PostMapping
    fun findUnallocatedForTeam(@Valid @RequestBody request: AllocationDemandRequest): AllocationDemandResponse =
        if (request.cases.isEmpty()) AllocationDemandResponse(listOf()) else allocationDemand.findAllocationDemand(request)

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("/choose-practitioner")
    fun choosePractitioner(
        @RequestParam crn: String,
        @RequestParam("teamCode", defaultValue = "") teamCodes: List<String> = listOf()
    ) = allocationDemand.getChoosePractitionerResponse(crn, teamCodes)

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("/{crn}/{eventNumber}/probation-record")
    fun getProbationRecord(@PathVariable crn: String, @PathVariable eventNumber: String) =
        allocationDemand.getProbationRecord(crn, eventNumber)

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("/impact")
    fun getImpact(@RequestParam crn: String, @RequestParam staff: String) = allocationDemand.getImpact(crn, staff)

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("/{crn}/{eventNumber}/case-view")
    fun caseView(@PathVariable crn: String, @PathVariable eventNumber: String) = allocationDemand.caseView(crn, eventNumber)

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("/{crn}/{eventNumber}/risk")
    fun getRisk(@PathVariable crn: String, @PathVariable eventNumber: String) =
        allocationRisk.getRiskRecord(crn, eventNumber)
}

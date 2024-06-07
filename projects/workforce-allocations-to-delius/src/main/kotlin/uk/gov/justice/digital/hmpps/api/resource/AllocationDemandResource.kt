package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
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

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = "List of summary probation case details for cases that require allocation",
        description = """Summary information on the probation case list provided in the request.
            Used to support the list of 'Unallocated Community Cases' in the HMPPS Workforce
            service which shows the list of cases that require allocation to a probation
            practitioner
        """
    )
    @PostMapping
    fun findUnallocatedForTeam(
        @Valid @RequestBody
        request: AllocationDemandRequest
    ): AllocationDemandResponse =
        if (request.cases.isEmpty()) {
            AllocationDemandResponse(listOf())
        } else {
            allocationDemand.findAllocationDemand(
                request
            )
        }

    @PreAuthorize("hasAnyRole('ALLOCATION_CONTEXT','PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = "List of summary probation case details with probation practioner details",
        description = """Summary information on the probation case provided in the request along
            with a list of probation practitioners associated with the teams provided in the
            request. Used to support the 'Choose Practitioner' screen of the HMPPS Workforce
            service which is part of the case allocation workflow
        """
    )
    @GetMapping("/choose-practitioner")
    fun choosePractitioner(
        @RequestParam crn: String,
        @RequestParam("teamCode", defaultValue = "") teamCodes: List<String> = listOf()
    ) = allocationDemand.getChoosePractitionerResponse(crn, teamCodes)

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = "Detailed information on the probation supervision history",
        description = """Detailed information on all current and previous cases, offences, sentences
            and management by probation practitioners for the person identified by the CRN and event
            provided in the request. Supports the 'Probation Record' screen of case allocation in
            the HMPPS Workforce service which is part of the case allocation workflow
        """
    )
    @GetMapping("/{crn}/{eventNumber}/probation-record")
    fun getProbationRecord(@PathVariable crn: String, @PathVariable eventNumber: String) =
        allocationDemand.getProbationRecord(crn, eventNumber)

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = "Summary information on the person on probation and probation practitioner",
        description = """Summary information on the person on probation and probation practitioner
            identified by the CRN and staff code provided in the request. Used to support the
            post-allocation 'Impact' screen of the HMPPS Workforce service
        """
    )
    @GetMapping("/impact")
    fun getImpact(@RequestParam crn: String, @RequestParam staff: String) = allocationDemand.getImpact(crn, staff)

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = "Detailed information on the risk information held in Delius",
        description = """Detailed information on the risk factors held and managed in Delius for the
            person on probation identified by the CRN provided in the request. Supports the 'Risk'
            section of the 'Case View' within the HMPPS Workload service which is part of the case
            allocation workflow
        """
    )
    @GetMapping("/{crn}/risk")
    fun getRisk(@PathVariable crn: String) = allocationRisk.getRiskRecord(crn)

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = """Summary information on all Delius events without a case allocation for a person
            on probation""",
        description = """Summary information on the person on probation identified by the CRN provided
            in the request with a list of all active Delius events for that person that do not currently
            have a case allocation. Used to support choosing the event to allocate in the case allocation
            workflow of the HMPPS Workforce service
        """
    )
    @GetMapping("/{crn}/unallocated-events")
    fun getUnallocatedEvents(@PathVariable crn: String) = allocationDemand.getUnallocatedEvents(crn)

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = "Detailed information on the case allocation",
        description = """Detailed information on the probation case, event and probation practitioners
            identified in the request. Used to display a summary page after case allocation has been
            completed in the HMPPS Workforce service
        """
    )
    @GetMapping("/{crn}/{eventNumber}/allocation")
    fun getAllocationDemandStaff(
        @PathVariable crn: String,
        @PathVariable eventNumber: String,
        @RequestParam staff: String,
        @RequestParam allocatingStaffUsername: String
    ) = allocationDemand.getAllocationDemandStaff(crn, eventNumber, staff, allocatingStaffUsername)
}

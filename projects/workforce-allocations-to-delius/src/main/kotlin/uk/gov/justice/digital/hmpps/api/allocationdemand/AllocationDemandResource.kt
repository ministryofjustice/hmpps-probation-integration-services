package uk.gov.justice.digital.hmpps.api.allocationdemand

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/allocation-demand")
class AllocationDemandResource(private val service: AllocationDemandService) {

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @PostMapping
    fun findUnallocatedForTeam(@Valid @RequestBody request: AllocationDemandRequest): AllocationDemandResponse =
        if (request.cases.isEmpty()) AllocationDemandResponse(listOf()) else service.findAllocationDemand(request)
}

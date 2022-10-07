package uk.gov.justice.digital.hmpps.api.allocationdemand

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/allocation-demand")
class AllocationDemandResource(private val service: AllocationDemandService) {

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @RequestMapping(method = [RequestMethod.GET, RequestMethod.POST])
    fun findUnallocatedForTeam(@Valid @RequestBody request: AllocationDemandRequest): AllocationDemandResponse =
        service.findAllocationDemand(request)
}

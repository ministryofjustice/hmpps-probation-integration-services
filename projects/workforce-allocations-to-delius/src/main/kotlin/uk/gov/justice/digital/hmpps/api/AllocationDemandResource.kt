package uk.gov.justice.digital.hmpps.api

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/allocation-demand")
class AllocationDemandResource() {

    @PreAuthorize("hasRole('ROLE_PI_WORKFORCE')")
    @GetMapping
    fun findUnallocatedForTeam(@RequestBody request: AllocationDemandRequest): AllocationDemandResponse {
        TODO()
    }
}
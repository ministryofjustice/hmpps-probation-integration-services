package uk.gov.justice.digital.hmpps.api.resource

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandRequest
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandResponse
import uk.gov.justice.digital.hmpps.service.AllocationDemandService

@Validated
@RestController
@RequestMapping("/allocation-demand")
class AllocationDemandResource(private val service: AllocationDemandService) {

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @PostMapping
    fun findUnallocatedForTeam(@Valid @RequestBody request: AllocationDemandRequest): AllocationDemandResponse =
        if (request.cases.isEmpty()) AllocationDemandResponse(listOf()) else service.findAllocationDemand(request)

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("/choose-practitioner")
    fun choosePractitioner(
        @RequestParam crn: String,
        @RequestParam("teamCode", defaultValue = "") teamCodes: List<String> = listOf()
    ) = service.getChoosePractitionerResponse(crn, teamCodes)
}

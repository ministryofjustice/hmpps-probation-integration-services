package uk.gov.justice.digital.hmpps.api.resource

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.AllocationDetailRequests
import uk.gov.justice.digital.hmpps.service.AllocationDemandService

@Validated
@RestController
@RequestMapping("/allocation")
class AllocationDetailResource(private val allocationDemand: AllocationDemandService) {
    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @PostMapping("/details")
    fun getDetails(@Valid @RequestBody detailRequests: AllocationDetailRequests) = allocationDemand.getDetails(detailRequests)
}
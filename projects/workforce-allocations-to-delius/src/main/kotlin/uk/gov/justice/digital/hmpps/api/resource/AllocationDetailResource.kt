package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
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
    @Operation(
        summary = "Summary information on the person on probation and probation practitioner",
        description = """Summary information on the person on probation and probation practitioner
            identified by the list of CRNs and staff codes provided in the request. Used to support
            ??
        """
    )
    @PostMapping("/details")
    fun getDetails(
        @Valid @RequestBody
        detailRequests: AllocationDetailRequests
    ) = allocationDemand.getDetails(detailRequests)
}

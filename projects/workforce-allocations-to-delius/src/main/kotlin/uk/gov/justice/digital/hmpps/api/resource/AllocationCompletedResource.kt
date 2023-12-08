package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.AllocationCompletedService

@Validated
@RestController
@RequestMapping("/allocation-completed")
class AllocationCompletedResource(private val service: AllocationCompletedService) {
    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @Operation(
        summary = "Summary case allocation information as currently held in Delius",
        description = """Summary case allocation information for the probation case
            identified by the CRN and Event Number in the request. Also provided
            information on the probation practitioner identified by the staff code
            in the request. Used to support the post-allocation information page of
            the HMPPS Workforce service
        """,
    )
    @GetMapping("/details")
    fun details(
        @RequestParam crn: String,
        @RequestParam eventNumber: String,
        @RequestParam staffCode: String,
    ) = service.getDetails(crn, eventNumber, staffCode)

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @Operation(
        summary = "Team allocation code for Persons PO",
        description = """
        """,
    )
    @GetMapping("/order-manager")
    fun getAllocatedManager(
        @RequestParam crn: String,
        @RequestParam eventNumber: String,
    ) = service.getAllocationManager(crn, eventNumber)
}

package uk.gov.justice.digital.hmpps.api.resource

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
    @GetMapping("/details")
    fun details(
        @RequestParam crn: String,
        @RequestParam eventNumber: String,
        @RequestParam staffCode: String,
    ) = service.getDetails(crn, eventNumber, staffCode)
}

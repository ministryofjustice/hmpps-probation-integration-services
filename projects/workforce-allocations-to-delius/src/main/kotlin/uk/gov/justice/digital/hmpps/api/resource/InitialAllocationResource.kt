package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.service.InitialAllocationService

@RestController
class InitialAllocationResource(
    private val initialAllocationService: InitialAllocationService
) {
    @Operation(summary = "A report of all allocations created by either the Manage a Workforce Allocation tool or Delius, since the start of 2024.")
    @GetMapping("/initial-allocations.csv", produces = ["text/csv"])
    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    fun getInitialAllocations(): ResponseEntity<StreamingResponseBody> = ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/csv"))
        .header("Content-Disposition", "attachment; filename=initial-allocations.csv")
        .body(StreamingResponseBody { initialAllocationService.writeInitialAllocations(it) })
}

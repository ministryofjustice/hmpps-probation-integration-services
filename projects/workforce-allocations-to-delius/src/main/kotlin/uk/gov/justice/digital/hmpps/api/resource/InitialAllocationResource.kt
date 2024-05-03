package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.config.CsvMapperConfig.csvMapper
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.InitialAllocation
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.InitialAllocationRepository

@RestController
class InitialAllocationResource(
    private val initialAllocationRepository: InitialAllocationRepository
) {
    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(summary = "A report of all allocations created by either the Manage a Workforce Allocation tool or Delius, since 2020.")
    @GetMapping("/initial-allocations.csv", produces = ["text/csv"])
    fun getInitialAllocations(): String = csvMapper
        .writer(csvMapper.schemaFor(InitialAllocation::class.java).withHeader())
        .writeValueAsString(initialAllocationRepository.findAllInitialAllocations())
}

package uk.gov.justice.digital.hmpps.api.model

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern

data class AllocationDemandRequest(@field:Valid val cases: List<AllocationRequest>)

data class AllocationRequest(
    @field:Pattern(regexp = "^[a-zA-Z]\\d{6}\$") val crn: String,
    @field:Pattern(regexp = "^\\d{1,4}\$") val eventNumber: String
)

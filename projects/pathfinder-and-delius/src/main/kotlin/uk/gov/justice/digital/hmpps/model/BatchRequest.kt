package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.Size

data class BatchRequest(
    @field:Size(min = 1, max = 500) val crns: List<String>,
)

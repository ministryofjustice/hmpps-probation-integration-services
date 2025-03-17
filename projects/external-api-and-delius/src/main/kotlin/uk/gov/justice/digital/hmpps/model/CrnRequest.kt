package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.Size

data class CrnRequest(
    @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") val crns: List<String>
)
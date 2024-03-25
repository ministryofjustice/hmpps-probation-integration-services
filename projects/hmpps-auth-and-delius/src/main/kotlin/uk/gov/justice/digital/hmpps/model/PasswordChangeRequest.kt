package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.NotBlank

data class PasswordChangeRequest(
    @field:NotBlank val password: String
)

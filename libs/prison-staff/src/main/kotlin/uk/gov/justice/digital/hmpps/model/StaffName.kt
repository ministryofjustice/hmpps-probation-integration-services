package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.NotBlank

data class StaffName(@NotBlank val forename: String, @NotBlank val surname: String)
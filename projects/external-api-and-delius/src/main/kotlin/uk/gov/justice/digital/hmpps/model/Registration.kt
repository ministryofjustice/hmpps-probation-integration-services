package uk.gov.justice.digital.hmpps.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class Registration(
    @Schema(example = "RCCO")
    val code: String,
    @Schema(example = "Child Concerns")
    val description: String,
    val startDate: LocalDate,
    val reviewDate: LocalDate?,
    val notes: String?
)
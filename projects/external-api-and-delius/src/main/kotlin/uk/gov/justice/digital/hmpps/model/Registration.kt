package uk.gov.justice.digital.hmpps.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class DynamicRiskRegistration(
    @Schema(example = "RCCO")
    val code: String,
    @Schema(example = "Child Concerns")
    val description: String,
    val startDate: LocalDate,
    val reviewDate: LocalDate?,
    val notes: String?
)

data class PersonStatusRegistration(
    @Schema(example = "ASFO")
    val code: String,
    @Schema(example = "Serious Further Offence - Subject to SFO review/investigation")
    val description: String,
    val startDate: LocalDate,
    val reviewDate: LocalDate?,
    val notes: String?
)
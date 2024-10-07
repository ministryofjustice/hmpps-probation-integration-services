package uk.gov.justice.digital.hmpps.model

import io.swagger.v3.oas.annotations.media.Schema

data class RefData(
    @Schema(description = "reference data code", example = "M")
    val code: String,
    @Schema(description = "reference data description", example = "MALE")
    val description: String
)

data class ProbationReferenceData(
    val probationReferenceData: Map<String, List<RefData>>
)
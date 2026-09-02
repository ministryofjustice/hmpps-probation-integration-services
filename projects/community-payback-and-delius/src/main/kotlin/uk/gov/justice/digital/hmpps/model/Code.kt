package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.NotBlank

data class Code(
    @field:NotBlank
    val code: String
)


data class CodeDescription(
    val code: String,
    val description: String
)

data class CodeName(
    val name: String,
    val code: String
)

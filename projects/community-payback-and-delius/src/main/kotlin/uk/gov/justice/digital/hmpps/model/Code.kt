package uk.gov.justice.digital.hmpps.model

data class Code(
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

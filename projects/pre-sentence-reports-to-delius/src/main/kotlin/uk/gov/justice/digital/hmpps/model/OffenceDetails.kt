package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class OffenceDetails(
    val mainOffence: Offence,
    val additionalOffences: List<Offence>
)

data class Offence(
    val date: LocalDate,
    val mainCategory: CodeAndDescription,
    val subCategory: CodeAndDescription
)

data class CodeAndDescription(val code: String, val description: String)
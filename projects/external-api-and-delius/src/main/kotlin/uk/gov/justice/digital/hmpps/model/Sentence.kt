package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Sentence(
    val description: String,
    val date: LocalDate,
    val length: Int?,
    val lengthUnits: LengthUnit?,
    val custodial: Boolean,
)

enum class LengthUnit {
    Hours,
    Days,
    Weeks,
    Months,
    Years,
}

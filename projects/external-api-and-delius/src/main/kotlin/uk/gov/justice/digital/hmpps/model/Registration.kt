package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Registration(
    val code: String,
    val description: String,
    val startDate: LocalDate,
    val reviewDate: LocalDate?,
    val notes: String?
)
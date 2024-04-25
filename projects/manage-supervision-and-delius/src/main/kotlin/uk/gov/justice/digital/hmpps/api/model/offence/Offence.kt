package uk.gov.justice.digital.hmpps.api.model.offence

import java.time.LocalDate

data class Offence(
    val description: String,
    val category: String,
    val code: String,
    val date: LocalDate?,
    val count: Long?,
    val notes: String?
)

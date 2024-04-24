package uk.gov.justice.digital.hmpps.api.model.offence

import java.time.LocalDate

data class Offence(
    val description: String,
    val category: String,
    val code: String,
    val dateOfOffence: LocalDate?,
    val count: Long?
)

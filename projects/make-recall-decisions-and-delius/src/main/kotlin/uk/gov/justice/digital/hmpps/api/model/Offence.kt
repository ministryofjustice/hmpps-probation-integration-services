package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class Offence(
    val date: LocalDate?,
    val code: String,
    val description: String,
)

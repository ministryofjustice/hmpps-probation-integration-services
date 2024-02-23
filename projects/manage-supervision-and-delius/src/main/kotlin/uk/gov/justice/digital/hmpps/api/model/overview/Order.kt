package uk.gov.justice.digital.hmpps.api.model.overview

import java.time.LocalDate

data class Order(
    val description: String,
    val endDate: LocalDate?,
    val startDate: LocalDate
)
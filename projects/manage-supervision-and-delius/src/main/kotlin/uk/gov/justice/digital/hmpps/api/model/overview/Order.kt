package uk.gov.justice.digital.hmpps.api.model.overview

import java.time.LocalDate

data class Order(
    val description: String,
    val length: Long?,
    val endDate: LocalDate?,
    val startDate: LocalDate,
    val status: String? = null,
    val mainOffence: String? = null,
    val breaches: Int? = 0
)
package uk.gov.justice.digital.hmpps.api.model.overview

import java.time.LocalDate

data class Order(
    val description: String,
    val length: Long? = null,
    val endDate: LocalDate?,
    val releaseDate: LocalDate? = null,
    val startDate: LocalDate,
    val status: String? = null,
    val mainOffence: String? = null,
    val breaches: Int? = 0
)
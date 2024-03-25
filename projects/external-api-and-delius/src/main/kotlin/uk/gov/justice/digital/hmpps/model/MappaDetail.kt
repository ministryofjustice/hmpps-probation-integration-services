package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class MappaDetail(
    val level: Int?,
    val levelDescription: String?,
    val category: Int?,
    val categoryDescription: String?,
    val startDate: LocalDate,
    val reviewDate: LocalDate?,
    val notes: String?
)
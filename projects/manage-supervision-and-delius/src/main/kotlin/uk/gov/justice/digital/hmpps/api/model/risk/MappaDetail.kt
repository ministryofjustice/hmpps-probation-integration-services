package uk.gov.justice.digital.hmpps.api.model.risk

import java.time.LocalDate

data class MappaDetail(
    val level: Int? = null,
    val levelDescription: String? = null,
    val category: Int? = null,
    val categoryDescription: String? = null,
    val startDate: LocalDate,
    val reviewDate: LocalDate? = null,
)

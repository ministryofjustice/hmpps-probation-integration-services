package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class CaseIdentifiers(
    val crn: String
)

data class MappaDetail(
    val level: Int?,
    val levelDescription: String?,
    val category: Int?,
    val categoryDescription: String?,
    val startDate: LocalDate,
    val reviewDate: LocalDate?
)

data class Manager(
    val name: Name?,
    val unallocated: Boolean
)

package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class RequirementsResponse(
    val requirements: List<CossoRequirement>,
    val breachReasons: List<CodeAndDescription>
)

data class CossoRequirement(
    val id: Long,
    val startDate: LocalDate,
    val mainCategory: String?,
    val subCategory: String?,
    val length: Int?,
    val lengthUnit: String?,
    val secondaryLength: Int?,
    val secondaryLengthUnit: String?,
)



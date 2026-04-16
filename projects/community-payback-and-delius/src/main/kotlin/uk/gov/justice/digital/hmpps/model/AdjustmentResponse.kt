package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.util.*

data class AdjustmentResponse(
    val adjustments: List<Adjustment>
)

data class Adjustment(
    val id: Long,
    val reference: UUID?,
    val type: AdjustmentType,
    val date: LocalDate,
    val reason: CodeName,
    val minutes: Int
)

data class AdjustmentPostResponse(
    val id: Long,
    val reference: UUID?,
)

enum class AdjustmentType(val code: String) {
    POSITIVE("POSITIVE"), NEGATIVE("NEGATIVE")
}



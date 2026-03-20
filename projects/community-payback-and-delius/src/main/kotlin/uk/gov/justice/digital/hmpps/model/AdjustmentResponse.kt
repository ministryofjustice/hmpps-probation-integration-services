package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class AdjustmentResponse(
    val adjustments: List<Adjustment>
)

data class Adjustment(
    val id: Long,
    val type: AdjustmentType,
    val date: LocalDate,
    val reason: AdjustmentReasonType,
    val minutes: Int
)

data class AdjustmentReasonType(
    val code: String,
    val name: String
)

data class AdjustmentPostResponse(
    val id: Long
)

enum class AdjustmentType(val code: String) {
    POSITIVE("POSITIVE"), NEGATIVE("NEGATIVE")
}



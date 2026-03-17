package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class AdjustmentResponse(
    val adjustments: List<Adjustment>
)

data class Adjustment(
    val id: Long,
    val reference: String,
    val adjustmentType: AdjustmentType,
    val date: LocalDate,
    val adjustmentReasonType: AdjustmentReasonType,
    val adjustmentAmountMinutes: Int
)

data class AdjustmentReasonType(
    val code: String,
    val name: String
)

data class AdjustmentPostResponse(
    val id: Long,
    val reference: String,
)

enum class AdjustmentType(val code: String) {
    POSITIVE("POSITIVE"), NEGATIVE("NEGATIVE")
}



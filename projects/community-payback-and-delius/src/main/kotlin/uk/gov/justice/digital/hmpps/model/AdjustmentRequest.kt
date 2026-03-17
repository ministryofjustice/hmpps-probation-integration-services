package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.util.UUID

data class AdjustmentRequest(
    val reference: UUID,
    val adjustmentType: AdjustmentType,
    val date: LocalDate,
    val adjustmentReasonTypeCode: String,
    val adjustmentAmountMinutes: Int
)
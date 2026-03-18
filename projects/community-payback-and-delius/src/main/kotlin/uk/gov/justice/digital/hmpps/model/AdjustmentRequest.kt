package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class AdjustmentRequest(
    val type: AdjustmentType,
    val date: LocalDate,
    val reasonTypeCode: String,
    val minutes: Int
)
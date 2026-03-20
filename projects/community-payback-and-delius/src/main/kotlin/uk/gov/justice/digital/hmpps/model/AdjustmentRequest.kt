package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class AdjustmentRequest(
    val crn: String,
    val eventNumber: Int,
    val type: AdjustmentType,
    val date: LocalDate,
    val reason: String,
    val minutes: Int
)
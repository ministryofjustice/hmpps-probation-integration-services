package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.util.*

data class CreateAdjustmentRequest(
    val reference: UUID,
    val crn: String,
    val eventNumber: Int,
    val type: AdjustmentType,
    val date: LocalDate,
    val reason: String,
    val minutes: Int
)

data class UpdateAdjustmentRequest(
    val crn: String,
    val eventNumber: Int,
    val type: AdjustmentType,
    val date: LocalDate,
    val reason: String,
    val minutes: Int
)
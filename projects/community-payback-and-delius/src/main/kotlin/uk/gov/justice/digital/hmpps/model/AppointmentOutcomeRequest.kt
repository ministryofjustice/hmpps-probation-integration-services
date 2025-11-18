package uk.gov.justice.digital.hmpps.model

import java.time.LocalTime
import java.util.*

data class AppointmentOutcomeRequest(
    val id: Long,
    val version: UUID,
    val outcome: Code?,
    val supervisor: Code?,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val notes: String?,
    val hiVisWorn: Boolean,
    val workedIntensively: Boolean,
    val penaltyMinutes: Long,
    val workQuality: String,
    val behaviour: String,
    val sensitive: Boolean,
    val alertActive: Boolean
)

package uk.gov.justice.digital.hmpps.model

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.*

data class CreateAppointmentsRequest(
    @field:Valid val appointments: List<CreateAppointmentRequest>
)

data class CreateAppointmentRequest(
    val reference: UUID,

    // person on probation
    @field:Pattern(regexp = "[A-Z][0-9]{6}")
    val crn: String,
    @field:Positive
    val eventNumber: Int,

    // schedule
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,

    // allocation
    val supervisor: Code,

    // unpaid work details
    val allocationId: Long?,
    val pickUp: PickUp?,
    @field:PositiveOrZero
    val minutesOffered: Long = ChronoUnit.MINUTES.between(startTime, endTime),

    // outcome details
    val outcome: Code?,
    @field:PositiveOrZero
    val penaltyMinutes: Long = 0,
    @field:PositiveOrZero
    val minutesCredited: Long = 0,
    val hiVisWorn: Boolean?,
    val workedIntensively: Boolean?,
    val workQuality: WorkQuality?,
    val behaviour: Behaviour?,

    // flags
    val sensitive: Boolean?,
    val alertActive: Boolean?,

    val notes: String?,
)
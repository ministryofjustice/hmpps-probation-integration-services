package uk.gov.justice.digital.hmpps.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.time.LocalDate
import java.time.LocalTime
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
    @Schema(description = "The appointment supervisor. Defaults to the unallocated staff member in the project team.")
    val supervisor: Code?,

    // unpaid work details
    val allocationId: Long?,
    val pickUp: PickUp?,
    @field:PositiveOrZero
    @Schema(description = "The minutes offered towards a person's unpaid work requirement. Defaults to the difference between the appointment startTime and endTime.")
    val minutesOffered: Long? = null,

    // outcome details
    val outcome: Code?,
    @field:PositiveOrZero
    val penaltyMinutes: Long? = null,
    @field:PositiveOrZero
    val minutesCredited: Long? = null,
    val hiVisWorn: Boolean?,
    val workedIntensively: Boolean?,
    val workQuality: WorkQuality?,
    val behaviour: Behaviour?,

    // flags
    val sensitive: Boolean?,
    val alertActive: Boolean?,

    val notes: String?,
)
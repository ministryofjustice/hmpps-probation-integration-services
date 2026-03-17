package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

data class AppointmentResponse(
    val id: Long,
    val reference: UUID?,
    val version: UUID,
    val project: Project,
    @Deprecated("Use project.type instead", ReplaceWith("project.type"))
    val projectType: CodeName,
    val case: Case,
    val event: EventResponse,
    val supervisor: Supervisor,
    val team: CodeName,
    val provider: CodeName,
    val pickUpData: PickUp?,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val penaltyHours: String,
    val minutesCredited: Long?,
    val outcome: CodeDescription?,
    val enforcementAction: AppointmentResponseEnforcementAction?,
    val hiVisWorn: Boolean?,
    val workedIntensively: Boolean?,
    val workQuality: WorkQuality?,
    val behaviour: Behaviour?,
    val notes: String?,
    val updatedAt: ZonedDateTime,
    val sensitive: Boolean?,
    val alertActive: Boolean?
)

data class AppointmentResponseEnforcementAction(
    val code: String,
    val description: String,
    val respondBy: LocalDate?
)

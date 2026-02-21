package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.LocalTime

data class AppointmentsResponse(
    val id: Long,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val daysOverdue: Long?,
    val case: AppointmentResponseCase,
    val requirementProgress: RequirementProgress,
    val outcome: CodeDescription?
)

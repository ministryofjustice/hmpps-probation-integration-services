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
    val eventNumber: Int?,
    val project: ProjectSummary,
    val requirementProgress: RequirementProgress,
    val outcome: CodeDescription?
)

data class ProjectSummary(
    val code: String,
    val name: String,
    val projectType: CodeDescription
)

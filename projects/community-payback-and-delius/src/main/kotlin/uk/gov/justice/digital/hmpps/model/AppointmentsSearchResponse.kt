package uk.gov.justice.digital.hmpps.model

import org.hibernate.query.Page
import java.time.LocalDate
import java.time.LocalTime

data class AppointmentsSearchResponse(
    val content: List<AppointmentsResponse>
)

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

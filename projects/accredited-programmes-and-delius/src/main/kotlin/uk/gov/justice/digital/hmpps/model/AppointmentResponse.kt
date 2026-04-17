package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class AppointmentResponse(
    val id: Long,
    val reference: String?,
    val crn: String,
    val requirementId: Long?,
    val licenceConditionId: Long?,
    val date: LocalDate,
    val startTime: ZonedDateTime?,
    val endTime: ZonedDateTime?,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    val type: CodedValue,
    val outcome: AppointmentOutcome?,
    val location: CodedValue?,
    val staff: ProbationPractitioner,
    val team: CodedValue,
    val notes: String?,
    val sensitive: Boolean?,
)

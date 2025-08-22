package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class AppointmentResponse(
    val crn: String,
    val reference: String?,
    val requirementId: Long?,
    val licenceConditionId: Long?,
    val date: LocalDate,
    val startTime: ZonedDateTime?,
    val endTime: ZonedDateTime?,
    val outcome: AppointmentOutcome?,
    val location: CodedValue?,
    val staff: ProbationPractitioner,
    val team: CodedValue,
    val notes: String?,
    val sensitive: Boolean,
)

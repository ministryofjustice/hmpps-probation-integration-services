package uk.gov.justice.digital.hmpps.api.model.appointment

import uk.gov.justice.digital.hmpps.api.model.user.Staff
import java.time.LocalDate
import java.time.LocalTime

data class OverdueOutcomeAppointments(val content: List<OverdueOutcome>)

data class OverdueOutcome(
    val id: Long,
    val externalReference: String?,
    val type: Type,
    val date: LocalDate,
    val start: LocalTime?,
    val end: LocalTime?,
    val staff: Staff
) {
    data class Type(val code: String, val description: String)
}
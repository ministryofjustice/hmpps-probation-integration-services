package uk.gov.justice.digital.hmpps.api.model.appointment

import java.time.LocalDate
import java.time.LocalTime

data class OverdueOutcomeAppointments(val content: List<OverdueOutcome>)

data class OverdueOutcome(
    val id: Long,
    val externalReference: String?,
    val type: Type,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
) {
    data class Type(val code: String, val description: String)
}
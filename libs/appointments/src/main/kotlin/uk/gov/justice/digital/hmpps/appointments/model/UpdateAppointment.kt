package uk.gov.justice.digital.hmpps.appointments.model

import java.time.LocalDate
import java.time.LocalTime

class UpdateAppointment {
    data class Outcome(
        val outcomeCode: String?,
    )

    data class Schedule(
        val date: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime?,
        val allowConflicts: Boolean = false,
    )

    data class RecreateAppointment(
        val date: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime?,
        val allowConflicts: Boolean = false,
        val rescheduledBy: RescheduledBy? = null,
        val newReference: String? = null,
    ) {
        enum class RescheduledBy(val outcomeCode: String) {
            PERSON_ON_PROBATION("RSOF"),
            PROBATION_SERVICE("RSSR"),
        }
    }

    data class Assignee(
        val staffCode: String,
        val teamCode: String,
        val locationCode: String?,
    )

    data class Flags(
        val sensitive: Boolean? = null,
        val visor: Boolean? = null,
    )
}
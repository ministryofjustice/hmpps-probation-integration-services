package uk.gov.justice.digital.hmpps.appointments.model

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

data class CreateAppointment(
    val reference: String,
    val typeCode: String,
    val relatedTo: ReferencedEntities,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime?,
    val staffCode: String,
    val teamCode: String,
    val locationCode: String? = null,
    val outcomeCode: String? = null,
    val notes: String? = null,
    val sensitive: Boolean? = false,
    val exportToVisor: Boolean? = false,
    val allowConflicts: Boolean = false,
) {
    init {
        require(endTime == null || startTime < endTime) {
            "Start time must be before end time"
        }

        require(outcomeCode != null || date.atTime(startTime).atZone(EuropeLondon) > ZonedDateTime.now()) {
            "Outcome must be provided when creating an appointment in the past"
        }
    }
}

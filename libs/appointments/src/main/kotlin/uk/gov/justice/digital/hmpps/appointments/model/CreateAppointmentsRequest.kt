package uk.gov.justice.digital.hmpps.appointments.model

import java.time.LocalDate
import java.time.LocalTime

data class CreateAppointmentRequest(
    val reference: String,
    val typeCode: String,
    val relatedTo: ReferencedEntities,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val staffCode: String,
    val teamCode: String,
    val locationCode: String? = null,
    val outcomeCode: String? = null,
    val notes: String? = null,
    val sensitive: Boolean = false,
    val exportToVisor: Boolean = false, // TODO for new contacts - validate that person has a MAPPA / ViSOR registration
)

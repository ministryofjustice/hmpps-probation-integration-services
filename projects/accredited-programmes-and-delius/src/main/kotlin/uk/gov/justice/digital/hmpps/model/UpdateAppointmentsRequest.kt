package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.NotEmpty
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class UpdateAppointmentsRequest(@NotEmpty val appointments: List<UpdateAppointmentRequest>) {
    fun findByReference(reference: String): UpdateAppointmentRequest? = appointments.firstOrNull {
        it.reference == UUID.fromString(reference.takeLast(36))
    }
}

data class UpdateAppointmentRequest(
    val reference: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val outcome: RequestCode?,
    val location: RequestCode?,
    val staff: RequestCode,
    val team: RequestCode,
    val notes: String?,
    val sensitive: Boolean
)
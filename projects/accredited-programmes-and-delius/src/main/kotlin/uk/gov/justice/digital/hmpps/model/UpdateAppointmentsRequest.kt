package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.NotEmpty
import uk.gov.justice.digital.hmpps.appointments.model.UpdateAppointment.Schedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

data class UpdateAppointmentsRequest(@NotEmpty val appointments: List<UpdateAppointmentRequest>)

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
    val sensitive: Boolean,
    val description: String?
) {
    fun within24Hrs() = Schedule(date, startTime, endTime).endDateTime >= ZonedDateTime.now().minusDays(1)
}
package uk.gov.justice.digital.hmpps.api.model.appointment

import java.time.LocalDate
import java.time.LocalTime

@FutureAppointment
@ValidAppointment
data class RescheduleAppointmentRequest(
    override val date: LocalDate,
    override val startTime: LocalTime,
    override val endTime: LocalTime,
    val staffCode: String?,
    val teamCode: String?,
    val locationCode: String?,
    val notes: String?,
    val sensitive: Boolean?,
) : AppointmentRequest

@FutureAppointment
@ValidAppointment
data class RecreateAppointmentRequest(
    override val date: LocalDate,
    override val startTime: LocalTime,
    override val endTime: LocalTime,
    val staffCode: String?,
    val teamCode: String?,
    val locationCode: String?,
    val notes: String?,
    val sensitive: Boolean?,
    val requestedBy: RequestedBy
) : AppointmentRequest {
    enum class RequestedBy {
        POP, SERVICE
    }
}

data class RecreatedAppointment(val id: Long)
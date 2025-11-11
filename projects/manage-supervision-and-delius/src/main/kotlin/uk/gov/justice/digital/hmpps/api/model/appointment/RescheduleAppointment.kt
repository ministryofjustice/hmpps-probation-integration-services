package uk.gov.justice.digital.hmpps.api.model.appointment

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

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
    val sendToVisor: Boolean?,
    val requestedBy: RequestedBy,
    val uuid: UUID?,
) : AppointmentRequest {
    enum class RequestedBy {
        POP, SERVICE
    }
}

data class RecreatedAppointment(val id: Long, val externalReference: String)
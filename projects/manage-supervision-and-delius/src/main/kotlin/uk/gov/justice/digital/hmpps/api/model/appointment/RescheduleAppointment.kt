package uk.gov.justice.digital.hmpps.api.model.appointment

import io.swagger.v3.oas.annotations.Parameter
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

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

@ValidAppointment
data class RecreateAppointmentRequest(
    override val date: LocalDate,
    override val startTime: LocalTime,
    override val endTime: LocalTime,
    val staffCode: String?,
    val teamCode: String?,
    val locationCode: String?,
    @Parameter(description = "Whether to record an attended/complied outcome for the new appointment. Required if appointment date/time is in the past")
    val outcomeRecorded: Boolean = false,
    val notes: String?,
    val sensitive: Boolean?,
    val sendToVisor: Boolean?,
    val requestedBy: RequestedBy,
    val reasonForRecreate: String?,
    val reasonIsSensitive: Boolean?,
    val uuid: UUID?,
) : AppointmentRequest {
    enum class RequestedBy {
        POP, SERVICE
    }
}

data class RecreatedAppointment(val id: Long, val externalReference: String)
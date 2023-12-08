package uk.gov.justice.digital.hmpps.integrations.randm

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.ZonedDateTime
import java.util.UUID

data class ReferralSession(
    val id: UUID,
    val appointmentId: UUID,
    val sessionNumber: Int,
    val appointmentTime: ZonedDateTime,
    @JsonAlias("deliusAppointmentId")
    val deliusId: Long?,
    val appointmentFeedback: AppointmentFeedback,
    val oldAppointments: List<Appointment>,
) {
    val latestFeedback: Appointment? =
        if (appointmentFeedback.attendanceFeedback.attended != null) {
            Appointment(appointmentId, appointmentFeedback)
        } else {
            oldAppointments.latestAttendanceRecorded()
        }
}

data class SupplierAssessment(
    val id: UUID,
    val appointments: List<Appointment>,
    val referralId: UUID,
) {
    val latestFeedback = appointments.latestAttendanceRecorded()
}

fun List<Appointment>.latestAttendanceRecorded() =
    filter(Appointment::attendanceRecorded).maxByOrNull { it.attendanceSubmittedAt()!! }

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
    val sessionFeedback: SessionFeedback,
    val oldAppointments: List<Appointment>
) {
    val latestFeedback: Appointment? =
        if (sessionFeedback.attendance.attended != null) {
            Appointment(appointmentId, sessionFeedback)
        } else {
            oldAppointments.filter { it.sessionFeedback.attendance.attended != null }
                .maxByOrNull { it.sessionFeedback.attendance.submittedAt!! }
        }
}

data class SessionFeedback(
    val attendance: Attendance,
    val behaviour: Behaviour
)

// Non-null behaviour with nullable attended to match model from Interventions Service
data class Attendance(
    val attended: String?,
    val submittedAt: ZonedDateTime?
)

// Non-null behaviour with nullable notify to match model from Interventions Service
data class Behaviour(
    val notifyProbationPractitioner: Boolean?
)

data class SupplierAssessment(
    val id: UUID,
    val appointments: List<Appointment>,
    val referralId: UUID
) {
    val latestFeedback =
        appointments.filter { it.sessionFeedback.attendance.attended != null }
            .maxByOrNull { it.sessionFeedback.attendance.submittedAt!! }
}

data class Appointment(
    val id: UUID,
    val sessionFeedback: SessionFeedback
)
